package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection baseline is in its life cycle. */
enum class PendingInspectionBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending inspection baseline, with what the planners need to act on it. */
data class PendingInspectionBaseline(
    var code: String,
    var title: String,
    var externalRef: String,
    var retries: Int,
    var ratio: Double,
    var shiftCode: String,
    var capacity: Int,
    var description: String,
    var endsAt: Int,
    var locale: String,
    var status: PendingInspectionBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionBaselineStatus): Boolean {
        when (status) {
            PendingInspectionBaselineStatus.REQUESTED -> {
                when (next) {
                    PendingInspectionBaselineStatus.REVOKED, PendingInspectionBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            PendingInspectionBaselineStatus.GRANTED -> return next == PendingInspectionBaselineStatus.REVOKED || next == PendingInspectionBaselineStatus.EXPIRED
            PendingInspectionBaselineStatus.REVOKED -> return next == PendingInspectionBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingInspectionBaselineStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending inspection baseline"} $code"

    fun weightedValue(): Double = round((ratio - retries.toDouble()) * 100) / 100

    fun snapshot(): PendingInspectionBaseline {
        return PendingInspectionBaseline(
            code = code,
            title = title,
            externalRef = externalRef,
            retries = retries,
            ratio = ratio,
            shiftCode = shiftCode,
            capacity = capacity,
            description = description,
            endsAt = endsAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending inspection baseline in status requested. */
fun newPendingInspectionBaseline(code: String, title: String): PendingInspectionBaseline {
    return PendingInspectionBaseline(
        code = code,
        title = title,
        externalRef = "",
        retries = 0,
        ratio = 0.0,
        shiftCode = "",
        capacity = 0,
        description = "",
        endsAt = 0,
        locale = "",
        status = PendingInspectionBaselineStatus.REQUESTED,
    )
}
