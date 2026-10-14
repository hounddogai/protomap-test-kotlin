package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default capacity exception is in its life cycle. */
enum class DefaultCapacityExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a default capacity exception and its current status. */
data class DefaultCapacityException(
    var code: String,
    var name: String,
    var odometer: Int,
    var ratio: Double,
    var source: String,
    var threshold: Double,
    var label: String,
    var fuelLiters: Double,
    var status: DefaultCapacityExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCapacityExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCapacityExceptionStatus): Boolean {
        when (status) {
            DefaultCapacityExceptionStatus.NEW -> {
                when (next) {
                    DefaultCapacityExceptionStatus.DONE -> return true
                }
                return false
            }
            DefaultCapacityExceptionStatus.QUEUED -> {
                when (next) {
                    DefaultCapacityExceptionStatus.DONE -> return true
                }
                return false
            }
            DefaultCapacityExceptionStatus.RUNNING -> return next == DefaultCapacityExceptionStatus.DONE || next == DefaultCapacityExceptionStatus.FAILED
            DefaultCapacityExceptionStatus.DONE -> {
                when (next) {
                    DefaultCapacityExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCapacityExceptionStatus) {
        if (!canMoveTo(next)) {
            error("default capacity exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default capacity exception"} $code"

    fun projectedTotal(): Double = round((fuelLiters - ratio) * 100) / 100

    fun snapshot(): DefaultCapacityException {
        return DefaultCapacityException(
            code = code,
            name = name,
            odometer = odometer,
            ratio = ratio,
            source = source,
            threshold = threshold,
            label = label,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default capacity exception in status new. */
fun newDefaultCapacityException(code: String, name: String): DefaultCapacityException {
    return DefaultCapacityException(
        code = code,
        name = name,
        odometer = 0,
        ratio = 0.0,
        source = "",
        threshold = 0.0,
        label = "",
        fuelLiters = 0.0,
        status = DefaultCapacityExceptionStatus.NEW,
    )
}
