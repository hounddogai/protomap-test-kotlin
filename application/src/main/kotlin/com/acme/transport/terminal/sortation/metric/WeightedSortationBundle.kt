package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted sortation bundle is in its life cycle. */
enum class WeightedSortationBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted sortation bundle. Its code is unique within a tenant. */
data class WeightedSortationBundle(
    var code: String,
    var name: String,
    var dock: String,
    var source: String,
    var odometer: Int,
    var fuelLiters: Double,
    var status: WeightedSortationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSortationBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSortationBundleStatus): Boolean {
        when (status) {
            WeightedSortationBundleStatus.IDLE -> {
                when (next) {
                    WeightedSortationBundleStatus.WARMING, WeightedSortationBundleStatus.READY -> return true
                }
                return false
            }
            WeightedSortationBundleStatus.WARMING -> {
                when (next) {
                    WeightedSortationBundleStatus.DRAINING -> return true
                }
                return false
            }
            WeightedSortationBundleStatus.READY -> {
                when (next) {
                    WeightedSortationBundleStatus.STOPPED -> return true
                }
                return false
            }
            WeightedSortationBundleStatus.DRAINING -> {
                when (next) {
                    WeightedSortationBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedSortationBundleStatus) {
        if (!canMoveTo(next)) {
            error("weighted sortation bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round((fuelLiters - odometer.toDouble()) * 100) / 100

    fun snapshot(): WeightedSortationBundle {
        return WeightedSortationBundle(
            code = code,
            name = name,
            dock = dock,
            source = source,
            odometer = odometer,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted sortation bundle in status idle. */
fun newWeightedSortationBundle(code: String, name: String): WeightedSortationBundle {
    return WeightedSortationBundle(
        code = code,
        name = name,
        dock = "",
        source = "",
        odometer = 0,
        fuelLiters = 0.0,
        status = WeightedSortationBundleStatus.IDLE,
    )
}
