package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive ev charge quota is in its life cycle. */
enum class AdaptiveEvChargeQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a adaptive ev charge quota and its current status. */
data class AdaptiveEvChargeQuota(
    var code: String,
    var label: String,
    var margin: Double,
    var amount: Double,
    var windowDays: Int,
    var contactEmail: String,
    var threshold: Double,
    var status: AdaptiveEvChargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEvChargeQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEvChargeQuotaStatus): Boolean {
        when (status) {
            AdaptiveEvChargeQuotaStatus.NEW -> return next == AdaptiveEvChargeQuotaStatus.QUEUED || next == AdaptiveEvChargeQuotaStatus.DONE
            AdaptiveEvChargeQuotaStatus.QUEUED -> {
                when (next) {
                    AdaptiveEvChargeQuotaStatus.DONE -> return true
                }
                return false
            }
            AdaptiveEvChargeQuotaStatus.RUNNING -> return next == AdaptiveEvChargeQuotaStatus.DONE || next == AdaptiveEvChargeQuotaStatus.FAILED
            AdaptiveEvChargeQuotaStatus.DONE -> return next == AdaptiveEvChargeQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveEvChargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("adaptive ev charge quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round((amount + threshold) * 100) / 100

    fun snapshot(): AdaptiveEvChargeQuota {
        return AdaptiveEvChargeQuota(
            code = code,
            label = label,
            margin = margin,
            amount = amount,
            windowDays = windowDays,
            contactEmail = contactEmail,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive ev charge quota in status new. */
fun newAdaptiveEvChargeQuota(code: String, label: String): AdaptiveEvChargeQuota {
    return AdaptiveEvChargeQuota(
        code = code,
        label = label,
        margin = 0.0,
        amount = 0.0,
        windowDays = 0,
        contactEmail = "",
        threshold = 0.0,
        status = AdaptiveEvChargeQuotaStatus.NEW,
    )
}
