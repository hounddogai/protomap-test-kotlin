package com.acme.transport.network.zone.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive zone record is in its life cycle. */
enum class AdaptiveZoneRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a adaptive zone record and its current status. */
data class AdaptiveZoneRecord(
    var code: String,
    var name: String,
    var maximum: Double,
    var carrier: String,
    var priority: Int,
    var depot: String,
    var minimum: Double,
    var amount: Double,
    var status: AdaptiveZoneRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveZoneRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveZoneRecordStatus): Boolean {
        when (status) {
            AdaptiveZoneRecordStatus.DRAFT -> return next == AdaptiveZoneRecordStatus.SUSPENDED
            AdaptiveZoneRecordStatus.PENDING -> {
                when (next) {
                    AdaptiveZoneRecordStatus.ACTIVE, AdaptiveZoneRecordStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveZoneRecordStatus.ACTIVE -> {
                when (next) {
                    AdaptiveZoneRecordStatus.SUSPENDED, AdaptiveZoneRecordStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveZoneRecordStatus.SUSPENDED -> return next == AdaptiveZoneRecordStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveZoneRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive zone record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round((maximum + priority.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveZoneRecord {
        return AdaptiveZoneRecord(
            code = code,
            name = name,
            maximum = maximum,
            carrier = carrier,
            priority = priority,
            depot = depot,
            minimum = minimum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive zone record in status draft. */
fun newAdaptiveZoneRecord(code: String, name: String): AdaptiveZoneRecord {
    return AdaptiveZoneRecord(
        code = code,
        name = name,
        maximum = 0.0,
        carrier = "",
        priority = 0,
        depot = "",
        minimum = 0.0,
        amount = 0.0,
        status = AdaptiveZoneRecordStatus.DRAFT,
    )
}
