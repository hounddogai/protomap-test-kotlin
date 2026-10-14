package com.acme.transport.customs.declaration.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending declaration forecast is in its life cycle. */
enum class PendingDeclarationForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending declaration forecast. Its code is unique within a tenant. */
data class PendingDeclarationForecast(
    var code: String,
    var title: String,
    var category: String,
    var routeCode: String,
    var limit: Int,
    var windowDays: Int,
    var ratio: Double,
    var name: String,
    var channel: String,
    var status: PendingDeclarationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDeclarationForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDeclarationForecastStatus): Boolean {
        when (status) {
            PendingDeclarationForecastStatus.PLANNED -> return next == PendingDeclarationForecastStatus.SCHEDULED || next == PendingDeclarationForecastStatus.IN_PROGRESS
            PendingDeclarationForecastStatus.SCHEDULED -> return next == PendingDeclarationForecastStatus.IN_PROGRESS || next == PendingDeclarationForecastStatus.COMPLETED
            PendingDeclarationForecastStatus.IN_PROGRESS -> return next == PendingDeclarationForecastStatus.COMPLETED || next == PendingDeclarationForecastStatus.CANCELLED
            PendingDeclarationForecastStatus.COMPLETED -> return next == PendingDeclarationForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingDeclarationForecastStatus) {
        if (!canMoveTo(next)) {
            error("pending declaration forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedLoad(): Double = round((ratio + windowDays.toDouble()) * 100) / 100

    fun snapshot(): PendingDeclarationForecast {
        return PendingDeclarationForecast(
            code = code,
            title = title,
            category = category,
            routeCode = routeCode,
            limit = limit,
            windowDays = windowDays,
            ratio = ratio,
            name = name,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending declaration forecast in status planned. */
fun newPendingDeclarationForecast(code: String, title: String): PendingDeclarationForecast {
    return PendingDeclarationForecast(
        code = code,
        title = title,
        category = "",
        routeCode = "",
        limit = 0,
        windowDays = 0,
        ratio = 0.0,
        name = "",
        channel = "",
        status = PendingDeclarationForecastStatus.PLANNED,
    )
}
