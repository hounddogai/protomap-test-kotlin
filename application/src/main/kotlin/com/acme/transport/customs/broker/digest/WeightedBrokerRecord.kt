package com.acme.transport.customs.broker.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted broker record is in its life cycle. */
enum class WeightedBrokerRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted broker record. Its code is unique within a tenant. */
data class WeightedBrokerRecord(
    var code: String,
    var name: String,
    var createdAt: Int,
    var loadKg: Double,
    var graceDays: Int,
    var category: String,
    var depot: String,
    var status: WeightedBrokerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBrokerRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBrokerRecordStatus): Boolean {
        when (status) {
            WeightedBrokerRecordStatus.PLANNED -> return next == WeightedBrokerRecordStatus.SCHEDULED || next == WeightedBrokerRecordStatus.CANCELLED
            WeightedBrokerRecordStatus.SCHEDULED -> {
                when (next) {
                    WeightedBrokerRecordStatus.IN_PROGRESS, WeightedBrokerRecordStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedBrokerRecordStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedBrokerRecordStatus.COMPLETED, WeightedBrokerRecordStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedBrokerRecordStatus.COMPLETED -> return next == WeightedBrokerRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedBrokerRecordStatus) {
        if (!canMoveTo(next)) {
            error("weighted broker record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((loadKg - createdAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedBrokerRecord {
        return WeightedBrokerRecord(
            code = code,
            name = name,
            createdAt = createdAt,
            loadKg = loadKg,
            graceDays = graceDays,
            category = category,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted broker record in status planned. */
fun newWeightedBrokerRecord(code: String, name: String): WeightedBrokerRecord {
    return WeightedBrokerRecord(
        code = code,
        name = name,
        createdAt = 0,
        loadKg = 0.0,
        graceDays = 0,
        category = "",
        depot = "",
        status = WeightedBrokerRecordStatus.PLANNED,
    )
}
