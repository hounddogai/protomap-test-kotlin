package com.acme.transport.safety.coaching.fallbackcoachingmetric

/** How screens and exports show a default coaching assignment. */
data class DefaultCoachingAssignmentView(
    var code: String,
    var label: String,
    var attempts: String,
    var externalRef: String,
    var approvedText: String,
    var createdAt: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toDefaultCoachingAssignmentView(item: DefaultCoachingAssignment): DefaultCoachingAssignmentView {
    return DefaultCoachingAssignmentView(
        code = item.code,
        label = item.label,
        attempts = item.attempts.toString(),
        externalRef = item.externalRef,
        approvedText = "${item.approved}",
        createdAt = item.createdAt.toString(),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCoachingAssignmentViews(items: MutableList<DefaultCoachingAssignment>): MutableList<DefaultCoachingAssignmentView> {
    val views = mutableListOf<DefaultCoachingAssignmentView>()
    for (item in items) {
        views.add(toDefaultCoachingAssignmentView(item))
    }
    return views
}

fun sumDefaultCoachingAssignmentView(view: DefaultCoachingAssignmentView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedAttempts.toDouble() + parsedCreatedAt.toDouble() + parsedScore
}
