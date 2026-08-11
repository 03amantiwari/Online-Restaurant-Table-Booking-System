package com.backend.restaurant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import com.backend.restaurantTable.entity.RestaurantTable;
import com.backend.timeSlot.entity.TimeSlot;
import com.backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "ownerUser")
public class Restaurant {
//restaurant_id,owner_user_id,restaurant_name,city,address,contact_number,email,price_band
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "restaurant_id")
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_restaurant_owner_user"))
	private User ownerUser;

	@NotBlank
	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@NotBlank
	@Column(name = "city", nullable = false)
	private String city;

	@NotBlank
	@Column(name = "address", nullable = false)
	private String address;

	@NotBlank
	@Column(name = "contact_number", nullable = false)
	private String contactNumber;

	@Email
	@Column(name = "contact_email")
	private String contactEmail;

	@Min(1)
	@Max(5)
	@Column(name = "price_band")
	private Integer priceBand;

	@Column(name = "active", nullable = false)
	private Boolean active;   // OPEN/CLOSED operational toggle — owner controls daily

	/*
	 * deleted — controls soft-delete / visibility to customers.
	 *
	 * WHY a separate field from active?
	 * active = false means "restaurant is CLOSED today" — customers can still
	 * SEE the card on the Home page but know it is closed.
	 *
	 * deleted = true means "owner has unlisted this restaurant" — the card
	 * must NOT appear on the Home page at all.
	 *
	 * Keeping them separate means: a CLOSED restaurant is still visible,
	 * a DELETED restaurant is completely hidden.
	 * Mixing both meanings into one boolean would make it impossible to
	 * have a restaurant that is CLOSED but still visible.
	 */
	@Column(name = "deleted", nullable = false)
	private Boolean deleted;  // false = visible to customers, true = hidden (soft-deleted)

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		if (this.active == null) {
			this.active = true;    // every new restaurant starts as OPEN
		}
		if (this.deleted == null) {
			this.deleted = false;  // every new restaurant starts as visible (not deleted)
		}
	}
	
	@OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestaurantTable> tables = new ArrayList<>();

    // 🔴 2. Restaurant -> TimeSlots Relationship (Cascade)
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimeSlot> timeSlots = new ArrayList<>();

    // Helper methods for setting bidirectional references in memory
    public void addTable(RestaurantTable table) {
        tables.add(table);
        table.setRestaurant(this);
    }

    public void addTimeSlot(TimeSlot timeSlot) {
        timeSlots.add(timeSlot);
        timeSlot.setRestaurant(this);
    }
}