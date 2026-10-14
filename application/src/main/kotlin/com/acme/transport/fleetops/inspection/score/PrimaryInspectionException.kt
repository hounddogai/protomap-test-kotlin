package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.text.normalize

/** Where a primary inspection exception is in its life cycle. */
enum class PrimaryInspectionExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a primary inspection exception and its current status. */
data class PrimaryInspectionException(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var checksum: String,
    var locale: String,
    var trailerId: String,
    var quantity: Int,
    var status: PrimaryInspectionExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionExceptionStatus): Boolean {
        when (status) {
            PrimaryInspectionExceptionStatus.CREATED -> {
                when (next) {
                    PrimaryInspectionExceptionStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryInspectionExceptionStatus.VALIDATED -> {
                when (next) {
                    PrimaryInspectionExceptionStatus.PUBLISHED, PrimaryInspectionExceptionStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryInspectionExceptionStatus.PUBLISHED -> return next == PrimaryInspectionExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionExceptionStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): PrimaryInspectionException {
        return PrimaryInspectionException(
            code = code,
            label = label,
            tags = tags,
            checksum = checksum,
            locale = locale,
            trailerId = trailerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary inspection exception in status created. */
fun newPrimaryInspectionException(code: String, label: String): PrimaryInspectionException {
    return PrimaryInspectionException(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        checksum = "",
        locale = "",
        trailerId = "",
        quantity = 0,
        status = PrimaryInspectionExceptionStatus.CREATED,
    )
}
