package com.acme.transport.safety.incident.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual incident exception records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a manual incident exception is in its life cycle. */
enum class ManualIncidentExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a manual incident exception and its current status. */
data class ManualIncidentException(
    var code: String,
    var title: String,
    var startsAt: Int,
    var batchSize: Int,
    var floorValue: Double,
    var externalRef: String,
    var maximum: Double,
    var status: ManualIncidentExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIncidentExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIncidentExceptionStatus): Boolean {
        when (status) {
            ManualIncidentExceptionStatus.CREATED -> return next == ManualIncidentExceptionStatus.VALIDATED || next == ManualIncidentExceptionStatus.PUBLISHED
            ManualIncidentExceptionStatus.VALIDATED -> return next == ManualIncidentExceptionStatus.PUBLISHED
            ManualIncidentExceptionStatus.PUBLISHED -> {
                when (next) {
                    ManualIncidentExceptionStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualIncidentExceptionStatus) {
        if (!canMoveTo(next)) {
            error("manual incident exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round(startsAt.toDouble() * maximum * 100) / 100

    fun snapshot(): ManualIncidentException {
        return ManualIncidentException(
            code = code,
            title = title,
            startsAt = startsAt,
            batchSize = batchSize,
            floorValue = floorValue,
            externalRef = externalRef,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual incident exception in status created. */
fun newManualIncidentException(code: String, title: String): ManualIncidentException {
    return ManualIncidentException(
        code = code,
        title = title,
        startsAt = 0,
        batchSize = 0,
        floorValue = 0.0,
        externalRef = "",
        maximum = 0.0,
        status = ManualIncidentExceptionStatus.CREATED,
    )
}
