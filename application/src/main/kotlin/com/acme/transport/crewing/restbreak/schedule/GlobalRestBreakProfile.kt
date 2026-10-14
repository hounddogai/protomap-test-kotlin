package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global rest break profile is in its life cycle. */
enum class GlobalRestBreakProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global rest break profile as the rest break team keeps it. */
data class GlobalRestBreakProfile(
    var code: String,
    var name: String,
    var capacity: Int,
    var threshold: Double,
    var phone: String,
    var amount: Double,
    var floorValue: Double,
    var reviewer: String,
    var title: String,
    var contactEmail: String,
    var status: GlobalRestBreakProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRestBreakProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRestBreakProfileStatus): Boolean {
        when (status) {
            GlobalRestBreakProfileStatus.OPEN -> {
                when (next) {
                    GlobalRestBreakProfileStatus.RESOLVED, GlobalRestBreakProfileStatus.REOPENED -> return true
                }
                return false
            }
            GlobalRestBreakProfileStatus.IN_REVIEW -> {
                when (next) {
                    GlobalRestBreakProfileStatus.RESOLVED, GlobalRestBreakProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalRestBreakProfileStatus.RESOLVED -> return next == GlobalRestBreakProfileStatus.REOPENED
            GlobalRestBreakProfileStatus.REOPENED -> {
                when (next) {
                    GlobalRestBreakProfileStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRestBreakProfileStatus) {
        if (!canMoveTo(next)) {
            error("global rest break profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round(floorValue * amount * 100) / 100

    fun snapshot(): GlobalRestBreakProfile {
        return GlobalRestBreakProfile(
            code = code,
            name = name,
            capacity = capacity,
            threshold = threshold,
            phone = phone,
            amount = amount,
            floorValue = floorValue,
            reviewer = reviewer,
            title = title,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global rest break profile in status open. */
fun newGlobalRestBreakProfile(code: String, name: String): GlobalRestBreakProfile {
    return GlobalRestBreakProfile(
        code = code,
        name = name,
        capacity = 0,
        threshold = 0.0,
        phone = "",
        amount = 0.0,
        floorValue = 0.0,
        reviewer = "",
        title = "",
        contactEmail = "",
        status = GlobalRestBreakProfileStatus.OPEN,
    )
}
