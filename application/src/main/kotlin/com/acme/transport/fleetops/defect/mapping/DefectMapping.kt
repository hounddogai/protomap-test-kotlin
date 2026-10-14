package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect mapping records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect mapping is in its life cycle. */
enum class DefectMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A defect mapping. Its code is unique within a tenant. */
data class DefectMapping(
    var code: String,
    var label: String,
    var endsAt: Int,
    var fuelLiters: Double,
    var revision: Int,
    var approved: Boolean,
    var notes: String,
    var pinned: Boolean,
    var minimum: Double,
    var status: DefectMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectMappingStatus): Boolean {
        when (status) {
            DefectMappingStatus.NEW -> return next == DefectMappingStatus.DONE
            DefectMappingStatus.QUEUED -> return next == DefectMappingStatus.RUNNING
            DefectMappingStatus.RUNNING -> {
                when (next) {
                    DefectMappingStatus.DONE, DefectMappingStatus.FAILED -> return true
                }
                return false
            }
            DefectMappingStatus.DONE -> return next == DefectMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefectMappingStatus) {
        if (!canMoveTo(next)) {
            error("defect mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"defect mapping"} $code"

    fun projectedVolume(): Double = round((endsAt.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): DefectMapping {
        return DefectMapping(
            code = code,
            label = label,
            endsAt = endsAt,
            fuelLiters = fuelLiters,
            revision = revision,
            approved = approved,
            notes = notes,
            pinned = pinned,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect mapping in status new. */
fun newDefectMapping(code: String, label: String): DefectMapping {
    return DefectMapping(
        code = code,
        label = label,
        endsAt = 0,
        fuelLiters = 0.0,
        revision = 0,
        approved = false,
        notes = "",
        pinned = false,
        minimum = 0.0,
        status = DefectMappingStatus.NEW,
    )
}
