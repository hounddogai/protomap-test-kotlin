package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered emission factor exception records of emissions's emission factor work, and the rules, stores,
// and calculations around them.

/** Where a tiered emission factor exception is in its life cycle. */
enum class TieredEmissionFactorExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered emission factor exception and its current status. */
data class TieredEmissionFactorException(
    var code: String,
    var title: String,
    var windowDays: Int,
    var quantity: Int,
    var odometer: Int,
    var minimum: Double,
    var country: String,
    var startsAt: Int,
    var weight: Double,
    var createdAt: Int,
    var ratio: Double,
    var status: TieredEmissionFactorExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEmissionFactorExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEmissionFactorExceptionStatus): Boolean {
        when (status) {
            TieredEmissionFactorExceptionStatus.REQUESTED -> return next == TieredEmissionFactorExceptionStatus.GRANTED || next == TieredEmissionFactorExceptionStatus.EXPIRED
            TieredEmissionFactorExceptionStatus.GRANTED -> {
                when (next) {
                    TieredEmissionFactorExceptionStatus.REVOKED, TieredEmissionFactorExceptionStatus.EXPIRED -> return true
                }
                return false
            }
            TieredEmissionFactorExceptionStatus.REVOKED -> return next == TieredEmissionFactorExceptionStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredEmissionFactorExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tiered emission factor exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedValue(): Double = round(windowDays.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): TieredEmissionFactorException {
        return TieredEmissionFactorException(
            code = code,
            title = title,
            windowDays = windowDays,
            quantity = quantity,
            odometer = odometer,
            minimum = minimum,
            country = country,
            startsAt = startsAt,
            weight = weight,
            createdAt = createdAt,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered emission factor exception in status requested. */
fun newTieredEmissionFactorException(code: String, title: String): TieredEmissionFactorException {
    return TieredEmissionFactorException(
        code = code,
        title = title,
        windowDays = 0,
        quantity = 0,
        odometer = 0,
        minimum = 0.0,
        country = "",
        startsAt = 0,
        weight = 0.0,
        createdAt = 0,
        ratio = 0.0,
        status = TieredEmissionFactorExceptionStatus.REQUESTED,
    )
}
