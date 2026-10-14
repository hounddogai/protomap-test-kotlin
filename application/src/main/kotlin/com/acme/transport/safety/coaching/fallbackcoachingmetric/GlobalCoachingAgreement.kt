package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global coaching agreement is in its life cycle. */
enum class GlobalCoachingAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global coaching agreement as the coaching team keeps it. */
data class GlobalCoachingAgreement(
    var code: String,
    var title: String,
    var floorValue: Double,
    var discount: Double,
    var retries: Int,
    var unit: String,
    var graceDays: Int,
    var durationMinutes: Int,
    var description: String,
    var region: String,
    var notes: String,
    var status: GlobalCoachingAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCoachingAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCoachingAgreementStatus): Boolean {
        when (status) {
            GlobalCoachingAgreementStatus.DRAFT -> return next == GlobalCoachingAgreementStatus.PENDING || next == GlobalCoachingAgreementStatus.CLOSED
            GlobalCoachingAgreementStatus.PENDING -> return next == GlobalCoachingAgreementStatus.ACTIVE || next == GlobalCoachingAgreementStatus.SUSPENDED
            GlobalCoachingAgreementStatus.ACTIVE -> return next == GlobalCoachingAgreementStatus.SUSPENDED || next == GlobalCoachingAgreementStatus.CLOSED
            GlobalCoachingAgreementStatus.SUSPENDED -> {
                when (next) {
                    GlobalCoachingAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCoachingAgreementStatus) {
        if (!canMoveTo(next)) {
            error("global coaching agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global coaching agreement"} $code"

    fun weightedValue(): Double = round((durationMinutes.toDouble() + discount) * 100) / 100

    fun snapshot(): GlobalCoachingAgreement {
        return GlobalCoachingAgreement(
            code = code,
            title = title,
            floorValue = floorValue,
            discount = discount,
            retries = retries,
            unit = unit,
            graceDays = graceDays,
            durationMinutes = durationMinutes,
            description = description,
            region = region,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global coaching agreement in status draft. */
fun newGlobalCoachingAgreement(code: String, title: String): GlobalCoachingAgreement {
    return GlobalCoachingAgreement(
        code = code,
        title = title,
        floorValue = 0.0,
        discount = 0.0,
        retries = 0,
        unit = "",
        graceDays = 0,
        durationMinutes = 0,
        description = "",
        region = "",
        notes = "",
        status = GlobalCoachingAgreementStatus.DRAFT,
    )
}
