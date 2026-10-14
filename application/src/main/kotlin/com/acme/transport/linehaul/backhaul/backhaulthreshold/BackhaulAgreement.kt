package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul agreement is in its life cycle. */
enum class BackhaulAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One backhaul agreement, with what the planners need to act on it. */
data class BackhaulAgreement(
    var code: String,
    var label: String,
    var externalRef: String,
    var updatedAt: Int,
    var ceiling: Double,
    var ratio: Double,
    var reviewer: String,
    var durationMinutes: Int,
    var endsAt: Int,
    var odometer: Int,
    var description: String,
    var status: BackhaulAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulAgreementStatus): Boolean {
        when (status) {
            BackhaulAgreementStatus.REQUESTED -> {
                when (next) {
                    BackhaulAgreementStatus.REVOKED -> return true
                }
                return false
            }
            BackhaulAgreementStatus.GRANTED -> return next == BackhaulAgreementStatus.REVOKED || next == BackhaulAgreementStatus.EXPIRED
            BackhaulAgreementStatus.REVOKED -> {
                when (next) {
                    BackhaulAgreementStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulAgreementStatus) {
        if (!canMoveTo(next)) {
            error("backhaul agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((updatedAt.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): BackhaulAgreement {
        return BackhaulAgreement(
            code = code,
            label = label,
            externalRef = externalRef,
            updatedAt = updatedAt,
            ceiling = ceiling,
            ratio = ratio,
            reviewer = reviewer,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            odometer = odometer,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a backhaul agreement in status requested. */
fun newBackhaulAgreement(code: String, label: String): BackhaulAgreement {
    return BackhaulAgreement(
        code = code,
        label = label,
        externalRef = "",
        updatedAt = 0,
        ceiling = 0.0,
        ratio = 0.0,
        reviewer = "",
        durationMinutes = 0,
        endsAt = 0,
        odometer = 0,
        description = "",
        status = BackhaulAgreementStatus.REQUESTED,
    )
}
