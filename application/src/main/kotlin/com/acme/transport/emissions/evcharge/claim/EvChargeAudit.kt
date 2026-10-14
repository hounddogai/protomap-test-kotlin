package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge audit is in its life cycle. */
enum class EvChargeAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One ev charge audit, with what the planners need to act on it. */
data class EvChargeAudit(
    var code: String,
    var label: String,
    var unit: String,
    var notes: String,
    var endsAt: Int,
    var rate: Double,
    var priority: Int,
    var pallets: Int,
    var durationMinutes: Int,
    var minimum: Double,
    var loadKg: Double,
    var status: EvChargeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeAuditStatus): Boolean {
        when (status) {
            EvChargeAuditStatus.REQUESTED -> {
                when (next) {
                    EvChargeAuditStatus.REVOKED, EvChargeAuditStatus.EXPIRED -> return true
                }
                return false
            }
            EvChargeAuditStatus.GRANTED -> return next == EvChargeAuditStatus.REVOKED || next == EvChargeAuditStatus.EXPIRED
            EvChargeAuditStatus.REVOKED -> return next == EvChargeAuditStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: EvChargeAuditStatus) {
        if (!canMoveTo(next)) {
            error("ev charge audit $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((durationMinutes.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): EvChargeAudit {
        return EvChargeAudit(
            code = code,
            label = label,
            unit = unit,
            notes = notes,
            endsAt = endsAt,
            rate = rate,
            priority = priority,
            pallets = pallets,
            durationMinutes = durationMinutes,
            minimum = minimum,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a ev charge audit in status requested. */
fun newEvChargeAudit(code: String, label: String): EvChargeAudit {
    return EvChargeAudit(
        code = code,
        label = label,
        unit = "",
        notes = "",
        endsAt = 0,
        rate = 0.0,
        priority = 0,
        pallets = 0,
        durationMinutes = 0,
        minimum = 0.0,
        loadKg = 0.0,
        status = EvChargeAuditStatus.REQUESTED,
    )
}
