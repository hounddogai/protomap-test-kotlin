package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual rate card preference is in its life cycle. */
enum class ManualRateCardPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual rate card preference, with what the planners need to act on it. */
data class ManualRateCardPreference(
    var code: String,
    var label: String,
    var checksum: String,
    var contactEmail: String,
    var margin: Double,
    var weight: Double,
    var depot: String,
    var createdAt: Int,
    var externalRef: String,
    var trailerId: String,
    var locale: String,
    var status: ManualRateCardPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRateCardPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRateCardPreferenceStatus): Boolean {
        when (status) {
            ManualRateCardPreferenceStatus.OPEN -> return next == ManualRateCardPreferenceStatus.REOPENED
            ManualRateCardPreferenceStatus.IN_REVIEW -> return next == ManualRateCardPreferenceStatus.RESOLVED || next == ManualRateCardPreferenceStatus.REOPENED
            ManualRateCardPreferenceStatus.RESOLVED -> return next == ManualRateCardPreferenceStatus.REOPENED || next == ManualRateCardPreferenceStatus.ARCHIVED
            ManualRateCardPreferenceStatus.REOPENED -> return next == ManualRateCardPreferenceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualRateCardPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("manual rate card preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round((margin + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ManualRateCardPreference {
        return ManualRateCardPreference(
            code = code,
            label = label,
            checksum = checksum,
            contactEmail = contactEmail,
            margin = margin,
            weight = weight,
            depot = depot,
            createdAt = createdAt,
            externalRef = externalRef,
            trailerId = trailerId,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual rate card preference in status open. */
fun newManualRateCardPreference(code: String, label: String): ManualRateCardPreference {
    return ManualRateCardPreference(
        code = code,
        label = label,
        checksum = "",
        contactEmail = "",
        margin = 0.0,
        weight = 0.0,
        depot = "",
        createdAt = 0,
        externalRef = "",
        trailerId = "",
        locale = "",
        status = ManualRateCardPreferenceStatus.OPEN,
    )
}
