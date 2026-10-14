package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident notice is in its life cycle. */
enum class IncidentNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A incident notice as the incident team keeps it. */
data class IncidentNotice(
    var code: String,
    var name: String,
    var currency: String,
    var createdAt: Int,
    var label: String,
    var vehicleId: String,
    var externalRef: String,
    var floorValue: Double,
    var startsAt: Int,
    var status: IncidentNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentNoticeStatus): Boolean {
        when (status) {
            IncidentNoticeStatus.NEW -> return next == IncidentNoticeStatus.DONE
            IncidentNoticeStatus.QUEUED -> {
                when (next) {
                    IncidentNoticeStatus.RUNNING -> return true
                }
                return false
            }
            IncidentNoticeStatus.RUNNING -> {
                when (next) {
                    IncidentNoticeStatus.DONE, IncidentNoticeStatus.FAILED -> return true
                }
                return false
            }
            IncidentNoticeStatus.DONE -> {
                when (next) {
                    IncidentNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentNoticeStatus) {
        if (!canMoveTo(next)) {
            error("incident notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $name"

    fun netExposure(): Double = round((createdAt.toDouble() + floorValue) * 100) / 100

    fun snapshot(): IncidentNotice {
        return IncidentNotice(
            code = code,
            name = name,
            currency = currency,
            createdAt = createdAt,
            label = label,
            vehicleId = vehicleId,
            externalRef = externalRef,
            floorValue = floorValue,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a incident notice in status new. */
fun newIncidentNotice(code: String, name: String): IncidentNotice {
    return IncidentNotice(
        code = code,
        name = name,
        currency = "",
        createdAt = 0,
        label = "",
        vehicleId = "",
        externalRef = "",
        floorValue = 0.0,
        startsAt = 0,
        status = IncidentNoticeStatus.NEW,
    )
}
