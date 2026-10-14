package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection threshold is in its life cycle. */
enum class AdaptiveInspectionThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive inspection threshold as the inspection team keeps it. */
data class AdaptiveInspectionThreshold(
    var code: String,
    var name: String,
    var externalRef: String,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var label: String,
    var createdAt: Int,
    var phone: String,
    var retries: Int,
    var status: AdaptiveInspectionThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionThresholdStatus): Boolean {
        when (status) {
            AdaptiveInspectionThresholdStatus.REQUESTED -> return next == AdaptiveInspectionThresholdStatus.REVOKED
            AdaptiveInspectionThresholdStatus.GRANTED -> return next == AdaptiveInspectionThresholdStatus.REVOKED || next == AdaptiveInspectionThresholdStatus.EXPIRED
            AdaptiveInspectionThresholdStatus.REVOKED -> {
                when (next) {
                    AdaptiveInspectionThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionThresholdStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"adaptive inspection threshold"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round(windowDays.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): AdaptiveInspectionThreshold {
        return AdaptiveInspectionThreshold(
            code = code,
            name = name,
            externalRef = externalRef,
            windowDays = windowDays,
            attributes = attributes,
            amount = amount,
            label = label,
            createdAt = createdAt,
            phone = phone,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive inspection threshold in status requested. */
fun newAdaptiveInspectionThreshold(code: String, name: String): AdaptiveInspectionThreshold {
    return AdaptiveInspectionThreshold(
        code = code,
        name = name,
        externalRef = "",
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        label = "",
        createdAt = 0,
        phone = "",
        retries = 0,
        status = AdaptiveInspectionThresholdStatus.REQUESTED,
    )
}
