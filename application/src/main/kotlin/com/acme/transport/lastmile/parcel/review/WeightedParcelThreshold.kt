package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted parcel threshold is in its life cycle. */
enum class WeightedParcelThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a weighted parcel threshold and its current status. */
data class WeightedParcelThreshold(
    var code: String,
    var title: String,
    var revision: Int,
    var rate: Double,
    var durationMinutes: Int,
    var pallets: Int,
    var status: WeightedParcelThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedParcelThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedParcelThresholdStatus): Boolean {
        when (status) {
            WeightedParcelThresholdStatus.DRAFT -> {
                when (next) {
                    WeightedParcelThresholdStatus.PENDING -> return true
                }
                return false
            }
            WeightedParcelThresholdStatus.PENDING -> {
                when (next) {
                    WeightedParcelThresholdStatus.ACTIVE, WeightedParcelThresholdStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedParcelThresholdStatus.ACTIVE -> return next == WeightedParcelThresholdStatus.SUSPENDED || next == WeightedParcelThresholdStatus.CLOSED
            WeightedParcelThresholdStatus.SUSPENDED -> {
                when (next) {
                    WeightedParcelThresholdStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedParcelThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted parcel threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"weighted parcel threshold"} $code"

    fun effectiveValue(): Double = round((pallets.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): WeightedParcelThreshold {
        return WeightedParcelThreshold(
            code = code,
            title = title,
            revision = revision,
            rate = rate,
            durationMinutes = durationMinutes,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted parcel threshold in status draft. */
fun newWeightedParcelThreshold(code: String, title: String): WeightedParcelThreshold {
    return WeightedParcelThreshold(
        code = code,
        title = title,
        revision = 0,
        rate = 0.0,
        durationMinutes = 0,
        pallets = 0,
        status = WeightedParcelThresholdStatus.DRAFT,
    )
}
