package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual broker override is in its life cycle. */
enum class ManualBrokerOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual broker override. Its code is unique within a tenant. */
data class ManualBrokerOverride(
    var code: String,
    var label: String,
    var loadKg: Double,
    var pallets: Int,
    var reviewer: String,
    var checksum: String,
    var floorValue: Double,
    var unit: String,
    var locale: String,
    var status: ManualBrokerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBrokerOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBrokerOverrideStatus): Boolean {
        when (status) {
            ManualBrokerOverrideStatus.REQUESTED -> return next == ManualBrokerOverrideStatus.REVOKED || next == ManualBrokerOverrideStatus.EXPIRED
            ManualBrokerOverrideStatus.GRANTED -> return next == ManualBrokerOverrideStatus.EXPIRED
            ManualBrokerOverrideStatus.REVOKED -> return next == ManualBrokerOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualBrokerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual broker override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((pallets.toDouble() - loadKg) * 100) / 100

    fun snapshot(): ManualBrokerOverride {
        return ManualBrokerOverride(
            code = code,
            label = label,
            loadKg = loadKg,
            pallets = pallets,
            reviewer = reviewer,
            checksum = checksum,
            floorValue = floorValue,
            unit = unit,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual broker override in status requested. */
fun newManualBrokerOverride(code: String, label: String): ManualBrokerOverride {
    return ManualBrokerOverride(
        code = code,
        label = label,
        loadKg = 0.0,
        pallets = 0,
        reviewer = "",
        checksum = "",
        floorValue = 0.0,
        unit = "",
        locale = "",
        status = ManualBrokerOverrideStatus.REQUESTED,
    )
}
