package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual hold profile is in its life cycle. */
enum class ManualHoldProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual hold profile. Its code is unique within a tenant. */
data class ManualHoldProfile(
    var code: String,
    var label: String,
    var floorValue: Double,
    var batchSize: Int,
    var title: String,
    var cost: Double,
    var distanceKm: Double,
    var maximum: Double,
    var vehicleId: String,
    var status: ManualHoldProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHoldProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHoldProfileStatus): Boolean {
        when (status) {
            ManualHoldProfileStatus.CREATED -> {
                when (next) {
                    ManualHoldProfileStatus.VALIDATED -> return true
                }
                return false
            }
            ManualHoldProfileStatus.VALIDATED -> {
                when (next) {
                    ManualHoldProfileStatus.RETIRED -> return true
                }
                return false
            }
            ManualHoldProfileStatus.PUBLISHED -> return next == ManualHoldProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualHoldProfileStatus) {
        if (!canMoveTo(next)) {
            error("manual hold profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((floorValue + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ManualHoldProfile {
        return ManualHoldProfile(
            code = code,
            label = label,
            floorValue = floorValue,
            batchSize = batchSize,
            title = title,
            cost = cost,
            distanceKm = distanceKm,
            maximum = maximum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual hold profile in status created. */
fun newManualHoldProfile(code: String, label: String): ManualHoldProfile {
    return ManualHoldProfile(
        code = code,
        label = label,
        floorValue = 0.0,
        batchSize = 0,
        title = "",
        cost = 0.0,
        distanceKm = 0.0,
        maximum = 0.0,
        vehicleId = "",
        status = ManualHoldProfileStatus.CREATED,
    )
}
