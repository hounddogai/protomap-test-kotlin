package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive audit bundle is in its life cycle. */
enum class AdaptiveAuditBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One adaptive audit bundle, with what the planners need to act on it. */
data class AdaptiveAuditBundle(
    var code: String,
    var label: String,
    var phone: String,
    var etaMinutes: Int,
    var notes: String,
    var channel: String,
    var locale: String,
    var odometer: Int,
    var status: AdaptiveAuditBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditBundleStatus): Boolean {
        when (status) {
            AdaptiveAuditBundleStatus.NEW -> return next == AdaptiveAuditBundleStatus.FAILED
            AdaptiveAuditBundleStatus.QUEUED -> {
                when (next) {
                    AdaptiveAuditBundleStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveAuditBundleStatus.RUNNING -> return next == AdaptiveAuditBundleStatus.DONE || next == AdaptiveAuditBundleStatus.FAILED
            AdaptiveAuditBundleStatus.DONE -> {
                when (next) {
                    AdaptiveAuditBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditBundleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round(etaMinutes.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): AdaptiveAuditBundle {
        return AdaptiveAuditBundle(
            code = code,
            label = label,
            phone = phone,
            etaMinutes = etaMinutes,
            notes = notes,
            channel = channel,
            locale = locale,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive audit bundle in status new. */
fun newAdaptiveAuditBundle(code: String, label: String): AdaptiveAuditBundle {
    return AdaptiveAuditBundle(
        code = code,
        label = label,
        phone = "",
        etaMinutes = 0,
        notes = "",
        channel = "",
        locale = "",
        odometer = 0,
        status = AdaptiveAuditBundleStatus.NEW,
    )
}
