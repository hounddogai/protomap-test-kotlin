package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted rest break rollup is in its life cycle. */
enum class WeightedRestBreakRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted rest break rollup. Its code is unique within a tenant. */
data class WeightedRestBreakRollup(
    var code: String,
    var label: String,
    var category: String,
    var discount: Double,
    var createdAt: Int,
    var checksum: String,
    var distanceKm: Double,
    var attributes: MutableMap<String, String>,
    var score: Double,
    var status: WeightedRestBreakRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRestBreakRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRestBreakRollupStatus): Boolean {
        when (status) {
            WeightedRestBreakRollupStatus.IDLE -> return next == WeightedRestBreakRollupStatus.READY || next == WeightedRestBreakRollupStatus.DRAINING
            WeightedRestBreakRollupStatus.WARMING -> return next == WeightedRestBreakRollupStatus.STOPPED
            WeightedRestBreakRollupStatus.READY -> {
                when (next) {
                    WeightedRestBreakRollupStatus.DRAINING, WeightedRestBreakRollupStatus.STOPPED -> return true
                }
                return false
            }
            WeightedRestBreakRollupStatus.DRAINING -> {
                when (next) {
                    WeightedRestBreakRollupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRestBreakRollupStatus) {
        if (!canMoveTo(next)) {
            error("weighted rest break rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted rest break rollup"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((distanceKm - createdAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedRestBreakRollup {
        return WeightedRestBreakRollup(
            code = code,
            label = label,
            category = category,
            discount = discount,
            createdAt = createdAt,
            checksum = checksum,
            distanceKm = distanceKm,
            attributes = attributes,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted rest break rollup in status idle. */
fun newWeightedRestBreakRollup(code: String, label: String): WeightedRestBreakRollup {
    return WeightedRestBreakRollup(
        code = code,
        label = label,
        category = "",
        discount = 0.0,
        createdAt = 0,
        checksum = "",
        distanceKm = 0.0,
        attributes = mutableMapOf<String, String>(),
        score = 0.0,
        status = WeightedRestBreakRollupStatus.IDLE,
    )
}
