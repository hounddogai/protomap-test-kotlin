package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.text.normalize

/** Where a scheduled redelivery profile is in its life cycle. */
enum class ScheduledRedeliveryProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled redelivery profile as the redelivery team keeps it. */
data class ScheduledRedeliveryProfile(
    var code: String,
    var title: String,
    var checksum: String,
    var tags: MutableList<String>,
    var approved: Boolean,
    var score: Double,
    var status: ScheduledRedeliveryProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRedeliveryProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRedeliveryProfileStatus): Boolean {
        when (status) {
            ScheduledRedeliveryProfileStatus.CREATED -> return next == ScheduledRedeliveryProfileStatus.PUBLISHED
            ScheduledRedeliveryProfileStatus.VALIDATED -> return next == ScheduledRedeliveryProfileStatus.PUBLISHED
            ScheduledRedeliveryProfileStatus.PUBLISHED -> return next == ScheduledRedeliveryProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledRedeliveryProfileStatus) {
        if (!canMoveTo(next)) {
            error("scheduled redelivery profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = score

    fun snapshot(): ScheduledRedeliveryProfile {
        return ScheduledRedeliveryProfile(
            code = code,
            title = title,
            checksum = checksum,
            tags = tags,
            approved = approved,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled redelivery profile in status created. */
fun newScheduledRedeliveryProfile(code: String, title: String): ScheduledRedeliveryProfile {
    return ScheduledRedeliveryProfile(
        code = code,
        title = title,
        checksum = "",
        tags = mutableListOf<String>(),
        approved = false,
        score = 0.0,
        status = ScheduledRedeliveryProfileStatus.CREATED,
    )
}
