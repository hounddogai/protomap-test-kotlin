package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual capacity cycle is in its life cycle. */
enum class ManualCapacityCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual capacity cycle. Its code is unique within a tenant. */
data class ManualCapacityCycle(
    var code: String,
    var title: String,
    var depot: String,
    var sequence: Int,
    var cost: Double,
    var driverId: String,
    var score: Double,
    var dock: String,
    var version: Int,
    var maximum: Double,
    var floorValue: Double,
    var status: ManualCapacityCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCapacityCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCapacityCycleStatus): Boolean {
        when (status) {
            ManualCapacityCycleStatus.CREATED -> return next == ManualCapacityCycleStatus.PUBLISHED || next == ManualCapacityCycleStatus.RETIRED
            ManualCapacityCycleStatus.VALIDATED -> return next == ManualCapacityCycleStatus.PUBLISHED || next == ManualCapacityCycleStatus.RETIRED
            ManualCapacityCycleStatus.PUBLISHED -> {
                when (next) {
                    ManualCapacityCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCapacityCycleStatus) {
        if (!canMoveTo(next)) {
            error("manual capacity cycle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun projectedVolume(): Double = round((maximum - cost) * 100) / 100

    fun snapshot(): ManualCapacityCycle {
        return ManualCapacityCycle(
            code = code,
            title = title,
            depot = depot,
            sequence = sequence,
            cost = cost,
            driverId = driverId,
            score = score,
            dock = dock,
            version = version,
            maximum = maximum,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual capacity cycle in status created. */
fun newManualCapacityCycle(code: String, title: String): ManualCapacityCycle {
    return ManualCapacityCycle(
        code = code,
        title = title,
        depot = "",
        sequence = 0,
        cost = 0.0,
        driverId = "",
        score = 0.0,
        dock = "",
        version = 0,
        maximum = 0.0,
        floorValue = 0.0,
        status = ManualCapacityCycleStatus.CREATED,
    )
}
