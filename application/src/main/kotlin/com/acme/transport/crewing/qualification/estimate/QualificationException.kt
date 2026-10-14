package com.acme.transport.crewing.qualification.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification exception is in its life cycle. */
enum class QualificationExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One qualification exception, with what the planners need to act on it. */
data class QualificationException(
    var code: String,
    var title: String,
    var active: Boolean,
    var ratio: Double,
    var phone: String,
    var trailerId: String,
    var notes: String,
    var weight: Double,
    var status: QualificationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationExceptionStatus): Boolean {
        when (status) {
            QualificationExceptionStatus.REQUESTED -> return next == QualificationExceptionStatus.EXPIRED
            QualificationExceptionStatus.GRANTED -> return next == QualificationExceptionStatus.REVOKED || next == QualificationExceptionStatus.EXPIRED
            QualificationExceptionStatus.REVOKED -> return next == QualificationExceptionStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: QualificationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("qualification exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((ratio + weight) * 100) / 100

    fun snapshot(): QualificationException {
        return QualificationException(
            code = code,
            title = title,
            active = active,
            ratio = ratio,
            phone = phone,
            trailerId = trailerId,
            notes = notes,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a qualification exception in status requested. */
fun newQualificationException(code: String, title: String): QualificationException {
    return QualificationException(
        code = code,
        title = title,
        active = false,
        ratio = 0.0,
        phone = "",
        trailerId = "",
        notes = "",
        weight = 0.0,
        status = QualificationExceptionStatus.REQUESTED,
    )
}
