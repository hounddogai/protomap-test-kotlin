package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted locker profile is in its life cycle. */
enum class WeightedLockerProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted locker profile. Its code is unique within a tenant. */
data class WeightedLockerProfile(
    var code: String,
    var title: String,
    var segment: String,
    var dock: String,
    var stopCount: Int,
    var label: String,
    var floorValue: Double,
    var budget: Double,
    var minimum: Double,
    var status: WeightedLockerProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLockerProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLockerProfileStatus): Boolean {
        when (status) {
            WeightedLockerProfileStatus.NEW -> {
                when (next) {
                    WeightedLockerProfileStatus.DONE, WeightedLockerProfileStatus.FAILED -> return true
                }
                return false
            }
            WeightedLockerProfileStatus.QUEUED -> return next == WeightedLockerProfileStatus.DONE || next == WeightedLockerProfileStatus.FAILED
            WeightedLockerProfileStatus.RUNNING -> {
                when (next) {
                    WeightedLockerProfileStatus.FAILED -> return true
                }
                return false
            }
            WeightedLockerProfileStatus.DONE -> {
                when (next) {
                    WeightedLockerProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedLockerProfileStatus) {
        if (!canMoveTo(next)) {
            error("weighted locker profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun effectiveLoad(): Double = round((floorValue + budget) * 100) / 100

    fun snapshot(): WeightedLockerProfile {
        return WeightedLockerProfile(
            code = code,
            title = title,
            segment = segment,
            dock = dock,
            stopCount = stopCount,
            label = label,
            floorValue = floorValue,
            budget = budget,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted locker profile in status new. */
fun newWeightedLockerProfile(code: String, title: String): WeightedLockerProfile {
    return WeightedLockerProfile(
        code = code,
        title = title,
        segment = "",
        dock = "",
        stopCount = 0,
        label = "",
        floorValue = 0.0,
        budget = 0.0,
        minimum = 0.0,
        status = WeightedLockerProfileStatus.NEW,
    )
}
