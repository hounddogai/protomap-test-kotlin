package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor segment is in its life cycle. */
enum class EmissionFactorSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A emission factor segment as the emission factor team keeps it. */
data class EmissionFactorSegment(
    var code: String,
    var name: String,
    var description: String,
    var fuelLiters: Double,
    var checksum: String,
    var ownerId: String,
    var tags: MutableList<String>,
    var label: String,
    var limit: Int,
    var status: EmissionFactorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorSegmentStatus): Boolean {
        when (status) {
            EmissionFactorSegmentStatus.OPEN -> {
                when (next) {
                    EmissionFactorSegmentStatus.IN_REVIEW, EmissionFactorSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            EmissionFactorSegmentStatus.IN_REVIEW -> return next == EmissionFactorSegmentStatus.RESOLVED
            EmissionFactorSegmentStatus.RESOLVED -> {
                when (next) {
                    EmissionFactorSegmentStatus.REOPENED, EmissionFactorSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            EmissionFactorSegmentStatus.REOPENED -> {
                when (next) {
                    EmissionFactorSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("emission factor segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((fuelLiters + limit.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorSegment {
        return EmissionFactorSegment(
            code = code,
            name = name,
            description = description,
            fuelLiters = fuelLiters,
            checksum = checksum,
            ownerId = ownerId,
            tags = tags,
            label = label,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a emission factor segment in status open. */
fun newEmissionFactorSegment(code: String, name: String): EmissionFactorSegment {
    return EmissionFactorSegment(
        code = code,
        name = name,
        description = "",
        fuelLiters = 0.0,
        checksum = "",
        ownerId = "",
        tags = mutableListOf<String>(),
        label = "",
        limit = 0,
        status = EmissionFactorSegmentStatus.OPEN,
    )
}
