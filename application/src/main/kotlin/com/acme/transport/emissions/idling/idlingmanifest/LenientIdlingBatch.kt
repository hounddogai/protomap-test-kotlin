package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient idling batch is in its life cycle. */
enum class LenientIdlingBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lenient idling batch and its current status. */
data class LenientIdlingBatch(
    var code: String,
    var label: String,
    var windowDays: Int,
    var unit: String,
    var floorValue: Double,
    var pallets: Int,
    var retries: Int,
    var status: LenientIdlingBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIdlingBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIdlingBatchStatus): Boolean {
        when (status) {
            LenientIdlingBatchStatus.REQUESTED -> return next == LenientIdlingBatchStatus.EXPIRED
            LenientIdlingBatchStatus.GRANTED -> {
                when (next) {
                    LenientIdlingBatchStatus.EXPIRED -> return true
                }
                return false
            }
            LenientIdlingBatchStatus.REVOKED -> return next == LenientIdlingBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientIdlingBatchStatus) {
        if (!canMoveTo(next)) {
            error("lenient idling batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round(pallets.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): LenientIdlingBatch {
        return LenientIdlingBatch(
            code = code,
            label = label,
            windowDays = windowDays,
            unit = unit,
            floorValue = floorValue,
            pallets = pallets,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient idling batch in status requested. */
fun newLenientIdlingBatch(code: String, label: String): LenientIdlingBatch {
    return LenientIdlingBatch(
        code = code,
        label = label,
        windowDays = 0,
        unit = "",
        floorValue = 0.0,
        pallets = 0,
        retries = 0,
        status = LenientIdlingBatchStatus.REQUESTED,
    )
}
