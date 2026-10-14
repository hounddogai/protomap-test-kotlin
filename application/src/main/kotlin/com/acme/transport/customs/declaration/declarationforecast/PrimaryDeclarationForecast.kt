package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary declaration forecast records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a primary declaration forecast is in its life cycle. */
enum class PrimaryDeclarationForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One primary declaration forecast, with what the planners need to act on it. */
data class PrimaryDeclarationForecast(
    var code: String,
    var name: String,
    var minimum: Double,
    var description: String,
    var notes: String,
    var loadKg: Double,
    var rate: Double,
    var floorValue: Double,
    var ceiling: Double,
    var status: PrimaryDeclarationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDeclarationForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDeclarationForecastStatus): Boolean {
        when (status) {
            PrimaryDeclarationForecastStatus.DRAFT -> {
                when (next) {
                    PrimaryDeclarationForecastStatus.PENDING, PrimaryDeclarationForecastStatus.ACTIVE -> return true
                }
                return false
            }
            PrimaryDeclarationForecastStatus.PENDING -> return next == PrimaryDeclarationForecastStatus.ACTIVE || next == PrimaryDeclarationForecastStatus.SUSPENDED
            PrimaryDeclarationForecastStatus.ACTIVE -> return next == PrimaryDeclarationForecastStatus.CLOSED
            PrimaryDeclarationForecastStatus.SUSPENDED -> return next == PrimaryDeclarationForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryDeclarationForecastStatus) {
        if (!canMoveTo(next)) {
            error("primary declaration forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round((ceiling - floorValue) * 100) / 100

    fun snapshot(): PrimaryDeclarationForecast {
        return PrimaryDeclarationForecast(
            code = code,
            name = name,
            minimum = minimum,
            description = description,
            notes = notes,
            loadKg = loadKg,
            rate = rate,
            floorValue = floorValue,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary declaration forecast in status draft. */
fun newPrimaryDeclarationForecast(code: String, name: String): PrimaryDeclarationForecast {
    return PrimaryDeclarationForecast(
        code = code,
        name = name,
        minimum = 0.0,
        description = "",
        notes = "",
        loadKg = 0.0,
        rate = 0.0,
        floorValue = 0.0,
        ceiling = 0.0,
        status = PrimaryDeclarationForecastStatus.DRAFT,
    )
}
