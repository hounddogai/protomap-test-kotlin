package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.normalize

/** Where a qualification threshold is in its life cycle. */
enum class QualificationThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One qualification threshold, with what the planners need to act on it. */
data class QualificationThreshold(
    var code: String,
    var name: String,
    var lane: String,
    var attempts: Int,
    var region: String,
    var approved: Boolean,
    var carrier: String,
    var timezone: String,
    var title: String,
    var status: QualificationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationThresholdStatus): Boolean {
        when (status) {
            QualificationThresholdStatus.NEW -> {
                when (next) {
                    QualificationThresholdStatus.RUNNING -> return true
                }
                return false
            }
            QualificationThresholdStatus.QUEUED -> {
                when (next) {
                    QualificationThresholdStatus.DONE, QualificationThresholdStatus.FAILED -> return true
                }
                return false
            }
            QualificationThresholdStatus.RUNNING -> return next == QualificationThresholdStatus.DONE || next == QualificationThresholdStatus.FAILED
            QualificationThresholdStatus.DONE -> {
                when (next) {
                    QualificationThresholdStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("qualification threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): QualificationThreshold {
        return QualificationThreshold(
            code = code,
            name = name,
            lane = lane,
            attempts = attempts,
            region = region,
            approved = approved,
            carrier = carrier,
            timezone = timezone,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a qualification threshold in status new. */
fun newQualificationThreshold(code: String, name: String): QualificationThreshold {
    return QualificationThreshold(
        code = code,
        name = name,
        lane = "",
        attempts = 0,
        region = "",
        approved = false,
        carrier = "",
        timezone = "",
        title = "",
        status = QualificationThresholdStatus.NEW,
    )
}
