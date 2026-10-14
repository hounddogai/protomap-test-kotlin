package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual backhaul baseline is in its life cycle. */
enum class ManualBackhaulBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual backhaul baseline as the backhaul team keeps it. */
data class ManualBackhaulBaseline(
    var code: String,
    var label: String,
    var rate: Double,
    var unit: String,
    var amount: Double,
    var retries: Int,
    var active: Boolean,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var contactEmail: String,
    var status: ManualBackhaulBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBackhaulBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBackhaulBaselineStatus): Boolean {
        when (status) {
            ManualBackhaulBaselineStatus.PROPOSED -> {
                when (next) {
                    ManualBackhaulBaselineStatus.APPROVED, ManualBackhaulBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualBackhaulBaselineStatus.APPROVED -> return next == ManualBackhaulBaselineStatus.REJECTED || next == ManualBackhaulBaselineStatus.WITHDRAWN
            ManualBackhaulBaselineStatus.REJECTED -> {
                when (next) {
                    ManualBackhaulBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualBackhaulBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual backhaul baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((retries.toDouble() + amount) * 100) / 100

    fun snapshot(): ManualBackhaulBaseline {
        return ManualBackhaulBaseline(
            code = code,
            label = label,
            rate = rate,
            unit = unit,
            amount = amount,
            retries = retries,
            active = active,
            attributes = attributes,
            vehicleId = vehicleId,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual backhaul baseline in status proposed. */
fun newManualBackhaulBaseline(code: String, label: String): ManualBackhaulBaseline {
    return ManualBackhaulBaseline(
        code = code,
        label = label,
        rate = 0.0,
        unit = "",
        amount = 0.0,
        retries = 0,
        active = false,
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        contactEmail = "",
        status = ManualBackhaulBaselineStatus.PROPOSED,
    )
}
