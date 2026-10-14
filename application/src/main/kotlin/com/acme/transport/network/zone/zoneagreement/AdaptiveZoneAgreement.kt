package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.kit.text.normalize

// The adaptive zone agreement records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a adaptive zone agreement is in its life cycle. */
enum class AdaptiveZoneAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a adaptive zone agreement and its current status. */
data class AdaptiveZoneAgreement(
    var code: String,
    var label: String,
    var phone: String,
    var source: String,
    var updatedAt: Int,
    var archived: Boolean,
    var status: AdaptiveZoneAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveZoneAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveZoneAgreementStatus): Boolean {
        when (status) {
            AdaptiveZoneAgreementStatus.OPEN -> {
                when (next) {
                    AdaptiveZoneAgreementStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveZoneAgreementStatus.IN_REVIEW -> return next == AdaptiveZoneAgreementStatus.RESOLVED || next == AdaptiveZoneAgreementStatus.REOPENED
            AdaptiveZoneAgreementStatus.RESOLVED -> return next == AdaptiveZoneAgreementStatus.REOPENED || next == AdaptiveZoneAgreementStatus.ARCHIVED
            AdaptiveZoneAgreementStatus.REOPENED -> {
                when (next) {
                    AdaptiveZoneAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveZoneAgreementStatus) {
        if (!canMoveTo(next)) {
            error("adaptive zone agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): AdaptiveZoneAgreement {
        return AdaptiveZoneAgreement(
            code = code,
            label = label,
            phone = phone,
            source = source,
            updatedAt = updatedAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive zone agreement in status open. */
fun newAdaptiveZoneAgreement(code: String, label: String): AdaptiveZoneAgreement {
    return AdaptiveZoneAgreement(
        code = code,
        label = label,
        phone = "",
        source = "",
        updatedAt = 0,
        archived = false,
        status = AdaptiveZoneAgreementStatus.OPEN,
    )
}
