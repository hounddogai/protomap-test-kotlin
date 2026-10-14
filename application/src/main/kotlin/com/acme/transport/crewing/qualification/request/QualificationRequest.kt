package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification request records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification request is in its life cycle. */
enum class QualificationRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a qualification request and its current status. */
data class QualificationRequest(
    var code: String,
    var label: String,
    var discount: Double,
    var sequence: Int,
    var locale: String,
    var channel: String,
    var visible: Boolean,
    var checksum: String,
    var fuelLiters: Double,
    var maximum: Double,
    var status: QualificationRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationRequestStatus): Boolean {
        when (status) {
            QualificationRequestStatus.OPEN -> {
                when (next) {
                    QualificationRequestStatus.RESOLVED -> return true
                }
                return false
            }
            QualificationRequestStatus.IN_REVIEW -> {
                when (next) {
                    QualificationRequestStatus.REOPENED -> return true
                }
                return false
            }
            QualificationRequestStatus.RESOLVED -> return next == QualificationRequestStatus.REOPENED || next == QualificationRequestStatus.ARCHIVED
            QualificationRequestStatus.REOPENED -> return next == QualificationRequestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: QualificationRequestStatus) {
        if (!canMoveTo(next)) {
            error("qualification request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"qualification request"} $code"

    fun weightedExposure(): Double = round(sequence.toDouble() * discount * 100) / 100

    fun snapshot(): QualificationRequest {
        return QualificationRequest(
            code = code,
            label = label,
            discount = discount,
            sequence = sequence,
            locale = locale,
            channel = channel,
            visible = visible,
            checksum = checksum,
            fuelLiters = fuelLiters,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a qualification request in status open. */
fun newQualificationRequest(code: String, label: String): QualificationRequest {
    return QualificationRequest(
        code = code,
        label = label,
        discount = 0.0,
        sequence = 0,
        locale = "",
        channel = "",
        visible = false,
        checksum = "",
        fuelLiters = 0.0,
        maximum = 0.0,
        status = QualificationRequestStatus.OPEN,
    )
}
