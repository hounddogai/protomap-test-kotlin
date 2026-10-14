package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect profile is in its life cycle. */
enum class DefectProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A defect profile as the defect team keeps it. */
data class DefectProfile(
    var code: String,
    var label: String,
    var maximum: Double,
    var createdAt: Int,
    var channel: String,
    var unit: String,
    var status: DefectProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectProfileStatus): Boolean {
        when (status) {
            DefectProfileStatus.CREATED -> return next == DefectProfileStatus.RETIRED
            DefectProfileStatus.VALIDATED -> {
                when (next) {
                    DefectProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            DefectProfileStatus.PUBLISHED -> {
                when (next) {
                    DefectProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectProfileStatus) {
        if (!canMoveTo(next)) {
            error("defect profile $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((createdAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): DefectProfile {
        return DefectProfile(
            code = code,
            label = label,
            maximum = maximum,
            createdAt = createdAt,
            channel = channel,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a defect profile in status created. */
fun newDefectProfile(code: String, label: String): DefectProfile {
    return DefectProfile(
        code = code,
        label = label,
        maximum = 0.0,
        createdAt = 0,
        channel = "",
        unit = "",
        status = DefectProfileStatus.CREATED,
    )
}
