package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan mapping is in its life cycle. */
enum class LanePlanMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lane plan mapping, with what the planners need to act on it. */
data class LanePlanMapping(
    var code: String,
    var label: String,
    var country: String,
    var checksum: String,
    var budget: Double,
    var vehicleId: String,
    var revision: Int,
    var sequence: Int,
    var odometer: Int,
    var contactEmail: String,
    var startsAt: Int,
    var status: LanePlanMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanMappingStatus): Boolean {
        when (status) {
            LanePlanMappingStatus.NEW -> return next == LanePlanMappingStatus.QUEUED || next == LanePlanMappingStatus.RUNNING
            LanePlanMappingStatus.QUEUED -> {
                when (next) {
                    LanePlanMappingStatus.FAILED -> return true
                }
                return false
            }
            LanePlanMappingStatus.RUNNING -> {
                when (next) {
                    LanePlanMappingStatus.DONE, LanePlanMappingStatus.FAILED -> return true
                }
                return false
            }
            LanePlanMappingStatus.DONE -> {
                when (next) {
                    LanePlanMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanMappingStatus) {
        if (!canMoveTo(next)) {
            error("lane plan mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round(odometer.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): LanePlanMapping {
        return LanePlanMapping(
            code = code,
            label = label,
            country = country,
            checksum = checksum,
            budget = budget,
            vehicleId = vehicleId,
            revision = revision,
            sequence = sequence,
            odometer = odometer,
            contactEmail = contactEmail,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lane plan mapping in status new. */
fun newLanePlanMapping(code: String, label: String): LanePlanMapping {
    return LanePlanMapping(
        code = code,
        label = label,
        country = "",
        checksum = "",
        budget = 0.0,
        vehicleId = "",
        revision = 0,
        sequence = 0,
        odometer = 0,
        contactEmail = "",
        startsAt = 0,
        status = LanePlanMappingStatus.NEW,
    )
}
