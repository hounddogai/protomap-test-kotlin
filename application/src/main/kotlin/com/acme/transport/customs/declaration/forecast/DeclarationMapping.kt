package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration mapping is in its life cycle. */
enum class DeclarationMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A declaration mapping. Its code is unique within a tenant. */
data class DeclarationMapping(
    var code: String,
    var label: String,
    var lane: String,
    var version: Int,
    var threshold: Double,
    var expiresAt: Int,
    var cost: Double,
    var minimum: Double,
    var externalRef: String,
    var status: DeclarationMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationMappingStatus): Boolean {
        when (status) {
            DeclarationMappingStatus.REQUESTED -> return next == DeclarationMappingStatus.GRANTED
            DeclarationMappingStatus.GRANTED -> return next == DeclarationMappingStatus.REVOKED
            DeclarationMappingStatus.REVOKED -> return next == DeclarationMappingStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DeclarationMappingStatus) {
        if (!canMoveTo(next)) {
            error("declaration mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((cost + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): DeclarationMapping {
        return DeclarationMapping(
            code = code,
            label = label,
            lane = lane,
            version = version,
            threshold = threshold,
            expiresAt = expiresAt,
            cost = cost,
            minimum = minimum,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a declaration mapping in status requested. */
fun newDeclarationMapping(code: String, label: String): DeclarationMapping {
    return DeclarationMapping(
        code = code,
        label = label,
        lane = "",
        version = 0,
        threshold = 0.0,
        expiresAt = 0,
        cost = 0.0,
        minimum = 0.0,
        externalRef = "",
        status = DeclarationMappingStatus.REQUESTED,
    )
}
