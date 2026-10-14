package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service plan exception is in its life cycle. */
enum class ServicePlanExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a service plan exception and its current status. */
data class ServicePlanException(
    var code: String,
    var label: String,
    var retries: Int,
    var shiftCode: String,
    var visible: Boolean,
    var attributes: MutableMap<String, String>,
    var driverId: String,
    var sequence: Int,
    var distanceKm: Double,
    var odometer: Int,
    var approved: Boolean,
    var status: ServicePlanExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServicePlanExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServicePlanExceptionStatus): Boolean {
        when (status) {
            ServicePlanExceptionStatus.NEW -> return next == ServicePlanExceptionStatus.FAILED
            ServicePlanExceptionStatus.QUEUED -> {
                when (next) {
                    ServicePlanExceptionStatus.RUNNING, ServicePlanExceptionStatus.FAILED -> return true
                }
                return false
            }
            ServicePlanExceptionStatus.RUNNING -> return next == ServicePlanExceptionStatus.FAILED
            ServicePlanExceptionStatus.DONE -> {
                when (next) {
                    ServicePlanExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServicePlanExceptionStatus) {
        if (!canMoveTo(next)) {
            error("service plan exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service plan exception"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round(distanceKm * retries.toDouble() * 100) / 100

    fun snapshot(): ServicePlanException {
        return ServicePlanException(
            code = code,
            label = label,
            retries = retries,
            shiftCode = shiftCode,
            visible = visible,
            attributes = attributes,
            driverId = driverId,
            sequence = sequence,
            distanceKm = distanceKm,
            odometer = odometer,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a service plan exception in status new. */
fun newServicePlanException(code: String, label: String): ServicePlanException {
    return ServicePlanException(
        code = code,
        label = label,
        retries = 0,
        shiftCode = "",
        visible = false,
        attributes = mutableMapOf<String, String>(),
        driverId = "",
        sequence = 0,
        distanceKm = 0.0,
        odometer = 0,
        approved = false,
        status = ServicePlanExceptionStatus.NEW,
    )
}
