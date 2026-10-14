package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization exception is in its life cycle. */
enum class UtilizationExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A utilization exception. Its code is unique within a tenant. */
data class UtilizationException(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var title: String,
    var source: String,
    var priority: Int,
    var ratio: Double,
    var loadKg: Double,
    var status: UtilizationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationExceptionStatus): Boolean {
        when (status) {
            UtilizationExceptionStatus.DRAFT -> {
                when (next) {
                    UtilizationExceptionStatus.PENDING, UtilizationExceptionStatus.CLOSED -> return true
                }
                return false
            }
            UtilizationExceptionStatus.PENDING -> {
                when (next) {
                    UtilizationExceptionStatus.ACTIVE, UtilizationExceptionStatus.CLOSED -> return true
                }
                return false
            }
            UtilizationExceptionStatus.ACTIVE -> return next == UtilizationExceptionStatus.CLOSED
            UtilizationExceptionStatus.SUSPENDED -> {
                when (next) {
                    UtilizationExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("utilization exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((loadKg - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): UtilizationException {
        return UtilizationException(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            title = title,
            source = source,
            priority = priority,
            ratio = ratio,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a utilization exception in status draft. */
fun newUtilizationException(code: String, label: String): UtilizationException {
    return UtilizationException(
        code = code,
        label = label,
        etaMinutes = 0,
        title = "",
        source = "",
        priority = 0,
        ratio = 0.0,
        loadKg = 0.0,
        status = UtilizationExceptionStatus.DRAFT,
    )
}
