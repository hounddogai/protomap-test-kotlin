package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification window is in its life cycle. */
enum class QualificationWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One qualification window, with what the planners need to act on it. */
data class QualificationWindow(
    var code: String,
    var name: String,
    var unit: String,
    var sequence: Int,
    var country: String,
    var limit: Int,
    var budget: Double,
    var approved: Boolean,
    var graceDays: Int,
    var status: QualificationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationWindowStatus): Boolean {
        when (status) {
            QualificationWindowStatus.DRAFT -> {
                when (next) {
                    QualificationWindowStatus.ACTIVE, QualificationWindowStatus.CLOSED -> return true
                }
                return false
            }
            QualificationWindowStatus.PENDING -> return next == QualificationWindowStatus.ACTIVE
            QualificationWindowStatus.ACTIVE -> {
                when (next) {
                    QualificationWindowStatus.CLOSED -> return true
                }
                return false
            }
            QualificationWindowStatus.SUSPENDED -> {
                when (next) {
                    QualificationWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationWindowStatus) {
        if (!canMoveTo(next)) {
            error("qualification window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"qualification window"} $code"

    fun projectedVolume(): Double = round((limit.toDouble() - budget) * 100) / 100

    fun snapshot(): QualificationWindow {
        return QualificationWindow(
            code = code,
            name = name,
            unit = unit,
            sequence = sequence,
            country = country,
            limit = limit,
            budget = budget,
            approved = approved,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a qualification window in status draft. */
fun newQualificationWindow(code: String, name: String): QualificationWindow {
    return QualificationWindow(
        code = code,
        name = name,
        unit = "",
        sequence = 0,
        country = "",
        limit = 0,
        budget = 0.0,
        approved = false,
        graceDays = 0,
        status = QualificationWindowStatus.DRAFT,
    )
}
