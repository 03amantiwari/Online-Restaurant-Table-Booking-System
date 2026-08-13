package com.backend.notification;


public final class RoutingKeys {

    private RoutingKeys() {
    }

    public static final String WORKSPACE_CREATED =
            "workspace.created";

    public static final String WORKSPACE_DELETED =
            "workspace.deleted";

    public static final String WORKSPACE_USER_ADDED =
            "workspace.user.added";

    public static final String WORKSPACE_USER_REMOVED =
            "workspace.user.removed";

    public static final String PROJECT_CREATED =
            "project.created";

    public static final String PROJECT_USER_ADDED =
            "project.user.added";

    public static final String TASK_ASSIGNED =
            "task.assigned";

}
