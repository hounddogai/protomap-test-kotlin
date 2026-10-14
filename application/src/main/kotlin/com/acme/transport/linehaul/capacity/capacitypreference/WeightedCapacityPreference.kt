package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted capacity preference records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a weighted capacity preference is in its life cycle. */
enum class WeightedCapacityPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted capacity preference. Its code is unique within a tenant. */
data class WeightedCapacityPreference(
    var code: String,
    var name: String,
    var checksum: String,
    var shiftCode: String,
    var budget: Double,
    var sequence: Int,
    var source: String,
    var carrier: String,
    var score: Double,
    var channel: String,
    var status: WeightedCapacityPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCapacityPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCapacityPreferenceStatus): Boolean {
        when (status) {
            WeightedCapacityPreferenceStatus.OPEN -> {
                when (next) {
                    WeightedCapacityPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedCapacityPreferenceStatus.IN_REVIEW -> return next == WeightedCapacityPreferenceStatus.RESOLVED || next == WeightedCapacityPreferenceStatus.REOPENED
            WeightedCapacityPreferenceStatus.RESOLVED -> {
                when (next) {
                    WeightedCapacityPreferenceStatus.REOPENED, WeightedCapacityPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedCapacityPreferenceStatus.REOPENED -> return next == WeightedCapacityPreferenceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedCapacityPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("weighted capacity preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round(sequence.toDouble() * score * 100) / 100

    fun snapshot(): WeightedCapacityPreference {
        return WeightedCapacityPreference(
            code = code,
            name = name,
            checksum = checksum,
            shiftCode = shiftCode,
            budget = budget,
            sequence = sequence,
            source = source,
            carrier = carrier,
            score = score,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted capacity preference in status open. */
fun newWeightedCapacityPreference(code: String, name: String): WeightedCapacityPreference {
    return WeightedCapacityPreference(
        code = code,
        name = name,
        checksum = "",
        shiftCode = "",
        budget = 0.0,
        sequence = 0,
        source = "",
        carrier = "",
        score = 0.0,
        channel = "",
        status = WeightedCapacityPreferenceStatus.OPEN,
    )
}
