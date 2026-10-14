package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted service area checkpoint is in its life cycle. */
enum class WeightedServiceAreaCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted service area checkpoint as the service area team keeps it. */
data class WeightedServiceAreaCheckpoint(
    var code: String,
    var name: String,
    var quantity: Int,
    var discount: Double,
    var trailerId: String,
    var carrier: String,
    var routeCode: String,
    var status: WeightedServiceAreaCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedServiceAreaCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedServiceAreaCheckpointStatus): Boolean {
        when (status) {
            WeightedServiceAreaCheckpointStatus.CREATED -> {
                when (next) {
                    WeightedServiceAreaCheckpointStatus.PUBLISHED -> return true
                }
                return false
            }
            WeightedServiceAreaCheckpointStatus.VALIDATED -> {
                when (next) {
                    WeightedServiceAreaCheckpointStatus.PUBLISHED, WeightedServiceAreaCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            WeightedServiceAreaCheckpointStatus.PUBLISHED -> {
                when (next) {
                    WeightedServiceAreaCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedServiceAreaCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("weighted service area checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted service area checkpoint"} $code"

    fun netValue(): Double = round((quantity.toDouble() - discount) * 100) / 100

    fun snapshot(): WeightedServiceAreaCheckpoint {
        return WeightedServiceAreaCheckpoint(
            code = code,
            name = name,
            quantity = quantity,
            discount = discount,
            trailerId = trailerId,
            carrier = carrier,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted service area checkpoint in status created. */
fun newWeightedServiceAreaCheckpoint(code: String, name: String): WeightedServiceAreaCheckpoint {
    return WeightedServiceAreaCheckpoint(
        code = code,
        name = name,
        quantity = 0,
        discount = 0.0,
        trailerId = "",
        carrier = "",
        routeCode = "",
        status = WeightedServiceAreaCheckpointStatus.CREATED,
    )
}
