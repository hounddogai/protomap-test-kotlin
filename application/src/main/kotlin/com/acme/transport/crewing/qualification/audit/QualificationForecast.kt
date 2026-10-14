package com.acme.transport.crewing.qualification.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification forecast is in its life cycle. */
enum class QualificationForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One qualification forecast, with what the planners need to act on it. */
data class QualificationForecast(
    var code: String,
    var name: String,
    var timezone: String,
    var maximum: Double,
    var dock: String,
    var label: String,
    var score: Double,
    var sequence: Int,
    var windowDays: Int,
    var country: String,
    var locale: String,
    var status: QualificationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationForecastStatus): Boolean {
        when (status) {
            QualificationForecastStatus.CREATED -> return next == QualificationForecastStatus.PUBLISHED || next == QualificationForecastStatus.RETIRED
            QualificationForecastStatus.VALIDATED -> return next == QualificationForecastStatus.PUBLISHED
            QualificationForecastStatus.PUBLISHED -> return next == QualificationForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: QualificationForecastStatus) {
        if (!canMoveTo(next)) {
            error("qualification forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"qualification forecast"} $code"

    fun effectiveExposure(): Double = round(sequence.toDouble() * score * 100) / 100

    fun snapshot(): QualificationForecast {
        return QualificationForecast(
            code = code,
            name = name,
            timezone = timezone,
            maximum = maximum,
            dock = dock,
            label = label,
            score = score,
            sequence = sequence,
            windowDays = windowDays,
            country = country,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a qualification forecast in status created. */
fun newQualificationForecast(code: String, name: String): QualificationForecast {
    return QualificationForecast(
        code = code,
        name = name,
        timezone = "",
        maximum = 0.0,
        dock = "",
        label = "",
        score = 0.0,
        sequence = 0,
        windowDays = 0,
        country = "",
        locale = "",
        status = QualificationForecastStatus.CREATED,
    )
}
