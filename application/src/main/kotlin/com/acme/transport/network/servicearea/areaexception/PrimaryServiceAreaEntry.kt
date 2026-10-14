package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary service area entry is in its life cycle. */
enum class PrimaryServiceAreaEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary service area entry. Its code is unique within a tenant. */
data class PrimaryServiceAreaEntry(
    var code: String,
    var label: String,
    var country: String,
    var maximum: Double,
    var amount: Double,
    var summary: String,
    var ownerId: String,
    var currency: String,
    var fuelLiters: Double,
    var createdAt: Int,
    var source: String,
    var status: PrimaryServiceAreaEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServiceAreaEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServiceAreaEntryStatus): Boolean {
        when (status) {
            PrimaryServiceAreaEntryStatus.PROPOSED -> {
                when (next) {
                    PrimaryServiceAreaEntryStatus.APPROVED -> return true
                }
                return false
            }
            PrimaryServiceAreaEntryStatus.APPROVED -> {
                when (next) {
                    PrimaryServiceAreaEntryStatus.REJECTED, PrimaryServiceAreaEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryServiceAreaEntryStatus.REJECTED -> {
                when (next) {
                    PrimaryServiceAreaEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryServiceAreaEntryStatus) {
        if (!canMoveTo(next)) {
            error("primary service area entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary service area entry"} $code"

    fun projectedVolume(): Double = round((amount - maximum) * 100) / 100

    fun snapshot(): PrimaryServiceAreaEntry {
        return PrimaryServiceAreaEntry(
            code = code,
            label = label,
            country = country,
            maximum = maximum,
            amount = amount,
            summary = summary,
            ownerId = ownerId,
            currency = currency,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary service area entry in status proposed. */
fun newPrimaryServiceAreaEntry(code: String, label: String): PrimaryServiceAreaEntry {
    return PrimaryServiceAreaEntry(
        code = code,
        label = label,
        country = "",
        maximum = 0.0,
        amount = 0.0,
        summary = "",
        ownerId = "",
        currency = "",
        fuelLiters = 0.0,
        createdAt = 0,
        source = "",
        status = PrimaryServiceAreaEntryStatus.PROPOSED,
    )
}
