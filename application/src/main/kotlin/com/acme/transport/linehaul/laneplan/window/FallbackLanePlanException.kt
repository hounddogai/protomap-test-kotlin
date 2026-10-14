package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback lane plan exception is in its life cycle. */
enum class FallbackLanePlanExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback lane plan exception and its current status. */
data class FallbackLanePlanException(
    var code: String,
    var label: String,
    var source: String,
    var score: Double,
    var loadKg: Double,
    var visible: Boolean,
    var etaMinutes: Int,
    var pallets: Int,
    var approved: Boolean,
    var contactEmail: String,
    var status: FallbackLanePlanExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLanePlanExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLanePlanExceptionStatus): Boolean {
        when (status) {
            FallbackLanePlanExceptionStatus.IDLE -> return next == FallbackLanePlanExceptionStatus.WARMING || next == FallbackLanePlanExceptionStatus.STOPPED
            FallbackLanePlanExceptionStatus.WARMING -> return next == FallbackLanePlanExceptionStatus.DRAINING || next == FallbackLanePlanExceptionStatus.STOPPED
            FallbackLanePlanExceptionStatus.READY -> return next == FallbackLanePlanExceptionStatus.DRAINING || next == FallbackLanePlanExceptionStatus.STOPPED
            FallbackLanePlanExceptionStatus.DRAINING -> {
                when (next) {
                    FallbackLanePlanExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackLanePlanExceptionStatus) {
        if (!canMoveTo(next)) {
            error("fallback lane plan exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((score - pallets.toDouble()) * 100) / 100

    fun snapshot(): FallbackLanePlanException {
        return FallbackLanePlanException(
            code = code,
            label = label,
            source = source,
            score = score,
            loadKg = loadKg,
            visible = visible,
            etaMinutes = etaMinutes,
            pallets = pallets,
            approved = approved,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback lane plan exception in status idle. */
fun newFallbackLanePlanException(code: String, label: String): FallbackLanePlanException {
    return FallbackLanePlanException(
        code = code,
        label = label,
        source = "",
        score = 0.0,
        loadKg = 0.0,
        visible = false,
        etaMinutes = 0,
        pallets = 0,
        approved = false,
        contactEmail = "",
        status = FallbackLanePlanExceptionStatus.IDLE,
    )
}
