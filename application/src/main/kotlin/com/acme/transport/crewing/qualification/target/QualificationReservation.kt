package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification reservation is in its life cycle. */
enum class QualificationReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A qualification reservation. Its code is unique within a tenant. */
data class QualificationReservation(
    var code: String,
    var title: String,
    var routeCode: String,
    var endsAt: Int,
    var batchSize: Int,
    var trailerId: String,
    var capacity: Int,
    var cost: Double,
    var source: String,
    var maximum: Double,
    var status: QualificationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationReservationStatus): Boolean {
        when (status) {
            QualificationReservationStatus.OPEN -> {
                when (next) {
                    QualificationReservationStatus.RESOLVED, QualificationReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            QualificationReservationStatus.IN_REVIEW -> return next == QualificationReservationStatus.REOPENED
            QualificationReservationStatus.RESOLVED -> return next == QualificationReservationStatus.ARCHIVED
            QualificationReservationStatus.REOPENED -> return next == QualificationReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: QualificationReservationStatus) {
        if (!canMoveTo(next)) {
            error("qualification reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"qualification reservation"} $code"

    fun adjustedLoad(): Double = round((maximum - batchSize.toDouble()) * 100) / 100

    fun snapshot(): QualificationReservation {
        return QualificationReservation(
            code = code,
            title = title,
            routeCode = routeCode,
            endsAt = endsAt,
            batchSize = batchSize,
            trailerId = trailerId,
            capacity = capacity,
            cost = cost,
            source = source,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a qualification reservation in status open. */
fun newQualificationReservation(code: String, title: String): QualificationReservation {
    return QualificationReservation(
        code = code,
        title = title,
        routeCode = "",
        endsAt = 0,
        batchSize = 0,
        trailerId = "",
        capacity = 0,
        cost = 0.0,
        source = "",
        maximum = 0.0,
        status = QualificationReservationStatus.OPEN,
    )
}
