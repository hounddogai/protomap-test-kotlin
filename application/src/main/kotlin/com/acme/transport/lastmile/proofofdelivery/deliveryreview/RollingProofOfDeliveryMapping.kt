package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling proof of delivery mapping is in its life cycle. */
enum class RollingProofOfDeliveryMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling proof of delivery mapping, with what the planners need to act on it. */
data class RollingProofOfDeliveryMapping(
    var code: String,
    var title: String,
    var priority: Int,
    var maximum: Double,
    var checksum: String,
    var depot: String,
    var name: String,
    var timezone: String,
    var minimum: Double,
    var capacity: Int,
    var updatedAt: Int,
    var status: RollingProofOfDeliveryMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingProofOfDeliveryMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingProofOfDeliveryMappingStatus): Boolean {
        when (status) {
            RollingProofOfDeliveryMappingStatus.PLANNED -> {
                when (next) {
                    RollingProofOfDeliveryMappingStatus.COMPLETED -> return true
                }
                return false
            }
            RollingProofOfDeliveryMappingStatus.SCHEDULED -> {
                when (next) {
                    RollingProofOfDeliveryMappingStatus.IN_PROGRESS, RollingProofOfDeliveryMappingStatus.COMPLETED -> return true
                }
                return false
            }
            RollingProofOfDeliveryMappingStatus.IN_PROGRESS -> return next == RollingProofOfDeliveryMappingStatus.COMPLETED || next == RollingProofOfDeliveryMappingStatus.CANCELLED
            RollingProofOfDeliveryMappingStatus.COMPLETED -> {
                when (next) {
                    RollingProofOfDeliveryMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingProofOfDeliveryMappingStatus) {
        if (!canMoveTo(next)) {
            error("rolling proof of delivery mapping $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling proof of delivery mapping"} $code"

    fun projectedLoad(): Double = round((maximum + minimum) * 100) / 100

    fun snapshot(): RollingProofOfDeliveryMapping {
        return RollingProofOfDeliveryMapping(
            code = code,
            title = title,
            priority = priority,
            maximum = maximum,
            checksum = checksum,
            depot = depot,
            name = name,
            timezone = timezone,
            minimum = minimum,
            capacity = capacity,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling proof of delivery mapping in status planned. */
fun newRollingProofOfDeliveryMapping(code: String, title: String): RollingProofOfDeliveryMapping {
    return RollingProofOfDeliveryMapping(
        code = code,
        title = title,
        priority = 0,
        maximum = 0.0,
        checksum = "",
        depot = "",
        name = "",
        timezone = "",
        minimum = 0.0,
        capacity = 0,
        updatedAt = 0,
        status = RollingProofOfDeliveryMappingStatus.PLANNED,
    )
}
