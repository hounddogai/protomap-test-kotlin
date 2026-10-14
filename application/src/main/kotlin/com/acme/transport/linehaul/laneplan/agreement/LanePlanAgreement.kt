package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan agreement records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a lane plan agreement is in its life cycle. */
enum class LanePlanAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lane plan agreement, with what the planners need to act on it. */
data class LanePlanAgreement(
    var code: String,
    var name: String,
    var archived: Boolean,
    var threshold: Double,
    var endsAt: Int,
    var notes: String,
    var loadKg: Double,
    var status: LanePlanAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanAgreementStatus): Boolean {
        when (status) {
            LanePlanAgreementStatus.PLANNED -> {
                when (next) {
                    LanePlanAgreementStatus.SCHEDULED -> return true
                }
                return false
            }
            LanePlanAgreementStatus.SCHEDULED -> return next == LanePlanAgreementStatus.COMPLETED
            LanePlanAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    LanePlanAgreementStatus.COMPLETED -> return true
                }
                return false
            }
            LanePlanAgreementStatus.COMPLETED -> return next == LanePlanAgreementStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LanePlanAgreementStatus) {
        if (!canMoveTo(next)) {
            error("lane plan agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lane plan agreement"} $code"

    fun effectiveExposure(): Double = round((loadKg + endsAt.toDouble()) * 100) / 100

    fun snapshot(): LanePlanAgreement {
        return LanePlanAgreement(
            code = code,
            name = name,
            archived = archived,
            threshold = threshold,
            endsAt = endsAt,
            notes = notes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lane plan agreement in status planned. */
fun newLanePlanAgreement(code: String, name: String): LanePlanAgreement {
    return LanePlanAgreement(
        code = code,
        name = name,
        archived = false,
        threshold = 0.0,
        endsAt = 0,
        notes = "",
        loadKg = 0.0,
        status = LanePlanAgreementStatus.PLANNED,
    )
}
