package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification cycle records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification cycle is in its life cycle. */
enum class QualificationCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One qualification cycle, with what the planners need to act on it. */
data class QualificationCycle(
    var code: String,
    var title: String,
    var quantity: Int,
    var source: String,
    var odometer: Int,
    var graceDays: Int,
    var currency: String,
    var status: QualificationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationCycleStatus): Boolean {
        when (status) {
            QualificationCycleStatus.CREATED -> {
                when (next) {
                    QualificationCycleStatus.RETIRED -> return true
                }
                return false
            }
            QualificationCycleStatus.VALIDATED -> {
                when (next) {
                    QualificationCycleStatus.RETIRED -> return true
                }
                return false
            }
            QualificationCycleStatus.PUBLISHED -> return next == QualificationCycleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: QualificationCycleStatus) {
        if (!canMoveTo(next)) {
            error("qualification cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"qualification cycle"} $code"

    fun projectedLoad(): Double = round(odometer.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): QualificationCycle {
        return QualificationCycle(
            code = code,
            title = title,
            quantity = quantity,
            source = source,
            odometer = odometer,
            graceDays = graceDays,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a qualification cycle in status created. */
fun newQualificationCycle(code: String, title: String): QualificationCycle {
    return QualificationCycle(
        code = code,
        title = title,
        quantity = 0,
        source = "",
        odometer = 0,
        graceDays = 0,
        currency = "",
        status = QualificationCycleStatus.CREATED,
    )
}
