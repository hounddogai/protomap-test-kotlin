package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor record records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor record is in its life cycle. */
enum class EmissionFactorRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A emission factor record as the emission factor team keeps it. */
data class EmissionFactorRecord(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var retries: Int,
    var odometer: Int,
    var tags: MutableList<String>,
    var status: EmissionFactorRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorRecordStatus): Boolean {
        when (status) {
            EmissionFactorRecordStatus.OPEN -> {
                when (next) {
                    EmissionFactorRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            EmissionFactorRecordStatus.IN_REVIEW -> return next == EmissionFactorRecordStatus.RESOLVED
            EmissionFactorRecordStatus.RESOLVED -> {
                when (next) {
                    EmissionFactorRecordStatus.REOPENED -> return true
                }
                return false
            }
            EmissionFactorRecordStatus.REOPENED -> return next == EmissionFactorRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: EmissionFactorRecordStatus) {
        if (!canMoveTo(next)) {
            error("emission factor record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"emission factor record"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(fuelLiters * retries.toDouble() * 100) / 100

    fun snapshot(): EmissionFactorRecord {
        return EmissionFactorRecord(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            retries = retries,
            odometer = odometer,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a emission factor record in status open. */
fun newEmissionFactorRecord(code: String, title: String): EmissionFactorRecord {
    return EmissionFactorRecord(
        code = code,
        title = title,
        fuelLiters = 0.0,
        retries = 0,
        odometer = 0,
        tags = mutableListOf<String>(),
        status = EmissionFactorRecordStatus.OPEN,
    )
}
