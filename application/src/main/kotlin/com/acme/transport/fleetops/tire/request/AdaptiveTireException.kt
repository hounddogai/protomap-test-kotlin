package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive tire exception is in its life cycle. */
enum class AdaptiveTireExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a adaptive tire exception and its current status. */
data class AdaptiveTireException(
    var code: String,
    var label: String,
    var archived: Boolean,
    var amount: Double,
    var visible: Boolean,
    var batchSize: Int,
    var active: Boolean,
    var segment: String,
    var status: AdaptiveTireExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTireExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTireExceptionStatus): Boolean {
        when (status) {
            AdaptiveTireExceptionStatus.CREATED -> {
                when (next) {
                    AdaptiveTireExceptionStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveTireExceptionStatus.VALIDATED -> {
                when (next) {
                    AdaptiveTireExceptionStatus.PUBLISHED, AdaptiveTireExceptionStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveTireExceptionStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveTireExceptionStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTireExceptionStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tire exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((amount + batchSize.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveTireException {
        return AdaptiveTireException(
            code = code,
            label = label,
            archived = archived,
            amount = amount,
            visible = visible,
            batchSize = batchSize,
            active = active,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive tire exception in status created. */
fun newAdaptiveTireException(code: String, label: String): AdaptiveTireException {
    return AdaptiveTireException(
        code = code,
        label = label,
        archived = false,
        amount = 0.0,
        visible = false,
        batchSize = 0,
        active = false,
        segment = "",
        status = AdaptiveTireExceptionStatus.CREATED,
    )
}
