package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit mapping records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit mapping is in its life cycle. */
enum class AuditMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a audit mapping and its current status. */
data class AuditMapping(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var attempts: Int,
    var loadKg: Double,
    var driverId: String,
    var reviewer: String,
    var region: String,
    var trailerId: String,
    var approved: Boolean,
    var status: AuditMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditMappingStatus): Boolean {
        when (status) {
            AuditMappingStatus.PROPOSED -> {
                when (next) {
                    AuditMappingStatus.APPROVED, AuditMappingStatus.REJECTED -> return true
                }
                return false
            }
            AuditMappingStatus.APPROVED -> return next == AuditMappingStatus.WITHDRAWN
            AuditMappingStatus.REJECTED -> {
                when (next) {
                    AuditMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditMappingStatus) {
        if (!canMoveTo(next)) {
            error("audit mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round((attempts.toDouble() - loadKg) * 100) / 100

    fun snapshot(): AuditMapping {
        return AuditMapping(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            attempts = attempts,
            loadKg = loadKg,
            driverId = driverId,
            reviewer = reviewer,
            region = region,
            trailerId = trailerId,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a audit mapping in status proposed. */
fun newAuditMapping(code: String, name: String): AuditMapping {
    return AuditMapping(
        code = code,
        name = name,
        fuelLiters = 0.0,
        attempts = 0,
        loadKg = 0.0,
        driverId = "",
        reviewer = "",
        region = "",
        trailerId = "",
        approved = false,
        status = AuditMappingStatus.PROPOSED,
    )
}
