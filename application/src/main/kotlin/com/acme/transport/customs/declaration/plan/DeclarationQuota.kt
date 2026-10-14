package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration quota is in its life cycle. */
enum class DeclarationQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A declaration quota as the declaration team keeps it. */
data class DeclarationQuota(
    var code: String,
    var label: String,
    var ratio: Double,
    var shiftCode: String,
    var country: String,
    var sequence: Int,
    var unit: String,
    var contactEmail: String,
    var ceiling: Double,
    var routeCode: String,
    var revision: Int,
    var status: DeclarationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationQuotaStatus): Boolean {
        when (status) {
            DeclarationQuotaStatus.IDLE -> {
                when (next) {
                    DeclarationQuotaStatus.DRAINING, DeclarationQuotaStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationQuotaStatus.WARMING -> return next == DeclarationQuotaStatus.READY || next == DeclarationQuotaStatus.DRAINING
            DeclarationQuotaStatus.READY -> {
                when (next) {
                    DeclarationQuotaStatus.DRAINING, DeclarationQuotaStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationQuotaStatus.DRAINING -> {
                when (next) {
                    DeclarationQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("declaration quota $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round((sequence.toDouble() + ceiling) * 100) / 100

    fun snapshot(): DeclarationQuota {
        return DeclarationQuota(
            code = code,
            label = label,
            ratio = ratio,
            shiftCode = shiftCode,
            country = country,
            sequence = sequence,
            unit = unit,
            contactEmail = contactEmail,
            ceiling = ceiling,
            routeCode = routeCode,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a declaration quota in status idle. */
fun newDeclarationQuota(code: String, label: String): DeclarationQuota {
    return DeclarationQuota(
        code = code,
        label = label,
        ratio = 0.0,
        shiftCode = "",
        country = "",
        sequence = 0,
        unit = "",
        contactEmail = "",
        ceiling = 0.0,
        routeCode = "",
        revision = 0,
        status = DeclarationQuotaStatus.IDLE,
    )
}
