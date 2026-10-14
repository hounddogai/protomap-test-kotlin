package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional declaration quota is in its life cycle. */
enum class RegionalDeclarationQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional declaration quota. Its code is unique within a tenant. */
data class RegionalDeclarationQuota(
    var code: String,
    var label: String,
    var segment: String,
    var margin: Double,
    var name: String,
    var fuelLiters: Double,
    var lane: String,
    var country: String,
    var updatedAt: Int,
    var ratio: Double,
    var notes: String,
    var status: RegionalDeclarationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDeclarationQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDeclarationQuotaStatus): Boolean {
        when (status) {
            RegionalDeclarationQuotaStatus.DRAFT -> {
                when (next) {
                    RegionalDeclarationQuotaStatus.PENDING, RegionalDeclarationQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalDeclarationQuotaStatus.PENDING -> return next == RegionalDeclarationQuotaStatus.SUSPENDED
            RegionalDeclarationQuotaStatus.ACTIVE -> return next == RegionalDeclarationQuotaStatus.CLOSED
            RegionalDeclarationQuotaStatus.SUSPENDED -> {
                when (next) {
                    RegionalDeclarationQuotaStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDeclarationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("regional declaration quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round((margin + fuelLiters) * 100) / 100

    fun snapshot(): RegionalDeclarationQuota {
        return RegionalDeclarationQuota(
            code = code,
            label = label,
            segment = segment,
            margin = margin,
            name = name,
            fuelLiters = fuelLiters,
            lane = lane,
            country = country,
            updatedAt = updatedAt,
            ratio = ratio,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional declaration quota in status draft. */
fun newRegionalDeclarationQuota(code: String, label: String): RegionalDeclarationQuota {
    return RegionalDeclarationQuota(
        code = code,
        label = label,
        segment = "",
        margin = 0.0,
        name = "",
        fuelLiters = 0.0,
        lane = "",
        country = "",
        updatedAt = 0,
        ratio = 0.0,
        notes = "",
        status = RegionalDeclarationQuotaStatus.DRAFT,
    )
}
