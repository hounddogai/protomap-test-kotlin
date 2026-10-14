package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted emission factor record is in its life cycle. */
enum class WeightedEmissionFactorRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One weighted emission factor record, with what the planners need to act on it. */
data class WeightedEmissionFactorRecord(
    var code: String,
    var name: String,
    var vehicleId: String,
    var ratio: Double,
    var sequence: Int,
    var pallets: Int,
    var discount: Double,
    var amount: Double,
    var status: WeightedEmissionFactorRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEmissionFactorRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEmissionFactorRecordStatus): Boolean {
        when (status) {
            WeightedEmissionFactorRecordStatus.REQUESTED -> {
                when (next) {
                    WeightedEmissionFactorRecordStatus.REVOKED -> return true
                }
                return false
            }
            WeightedEmissionFactorRecordStatus.GRANTED -> {
                when (next) {
                    WeightedEmissionFactorRecordStatus.REVOKED, WeightedEmissionFactorRecordStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedEmissionFactorRecordStatus.REVOKED -> return next == WeightedEmissionFactorRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedEmissionFactorRecordStatus) {
        if (!canMoveTo(next)) {
            error("weighted emission factor record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedExposure(): Double = round((discount - pallets.toDouble()) * 100) / 100

    fun snapshot(): WeightedEmissionFactorRecord {
        return WeightedEmissionFactorRecord(
            code = code,
            name = name,
            vehicleId = vehicleId,
            ratio = ratio,
            sequence = sequence,
            pallets = pallets,
            discount = discount,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted emission factor record in status requested. */
fun newWeightedEmissionFactorRecord(code: String, name: String): WeightedEmissionFactorRecord {
    return WeightedEmissionFactorRecord(
        code = code,
        name = name,
        vehicleId = "",
        ratio = 0.0,
        sequence = 0,
        pallets = 0,
        discount = 0.0,
        amount = 0.0,
        status = WeightedEmissionFactorRecordStatus.REQUESTED,
    )
}
