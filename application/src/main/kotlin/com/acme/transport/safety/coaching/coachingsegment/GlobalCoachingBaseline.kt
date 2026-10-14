package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global coaching baseline is in its life cycle. */
enum class GlobalCoachingBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global coaching baseline as the coaching team keeps it. */
data class GlobalCoachingBaseline(
    var code: String,
    var title: String,
    var category: String,
    var label: String,
    var contactEmail: String,
    var endsAt: Int,
    var reviewer: String,
    var threshold: Double,
    var timezone: String,
    var status: GlobalCoachingBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCoachingBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCoachingBaselineStatus): Boolean {
        when (status) {
            GlobalCoachingBaselineStatus.REQUESTED -> {
                when (next) {
                    GlobalCoachingBaselineStatus.GRANTED, GlobalCoachingBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalCoachingBaselineStatus.GRANTED -> return next == GlobalCoachingBaselineStatus.REVOKED || next == GlobalCoachingBaselineStatus.EXPIRED
            GlobalCoachingBaselineStatus.REVOKED -> return next == GlobalCoachingBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalCoachingBaselineStatus) {
        if (!canMoveTo(next)) {
            error("global coaching baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"global coaching baseline"} $code"

    fun netValue(): Double = round(threshold * endsAt.toDouble() * 100) / 100

    fun snapshot(): GlobalCoachingBaseline {
        return GlobalCoachingBaseline(
            code = code,
            title = title,
            category = category,
            label = label,
            contactEmail = contactEmail,
            endsAt = endsAt,
            reviewer = reviewer,
            threshold = threshold,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global coaching baseline in status requested. */
fun newGlobalCoachingBaseline(code: String, title: String): GlobalCoachingBaseline {
    return GlobalCoachingBaseline(
        code = code,
        title = title,
        category = "",
        label = "",
        contactEmail = "",
        endsAt = 0,
        reviewer = "",
        threshold = 0.0,
        timezone = "",
        status = GlobalCoachingBaselineStatus.REQUESTED,
    )
}
