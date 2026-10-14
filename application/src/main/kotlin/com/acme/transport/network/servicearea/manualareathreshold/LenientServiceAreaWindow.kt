package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient service area window is in its life cycle. */
enum class LenientServiceAreaWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient service area window as the service area team keeps it. */
data class LenientServiceAreaWindow(
    var code: String,
    var title: String,
    var externalRef: String,
    var stopCount: Int,
    var endsAt: Int,
    var amount: Double,
    var expiresAt: Int,
    var checksum: String,
    var attempts: Int,
    var country: String,
    var sequence: Int,
    var status: LenientServiceAreaWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServiceAreaWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServiceAreaWindowStatus): Boolean {
        when (status) {
            LenientServiceAreaWindowStatus.CREATED -> return next == LenientServiceAreaWindowStatus.VALIDATED || next == LenientServiceAreaWindowStatus.RETIRED
            LenientServiceAreaWindowStatus.VALIDATED -> return next == LenientServiceAreaWindowStatus.PUBLISHED || next == LenientServiceAreaWindowStatus.RETIRED
            LenientServiceAreaWindowStatus.PUBLISHED -> return next == LenientServiceAreaWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientServiceAreaWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient service area window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient service area window"} $code"

    fun weightedVolume(): Double = round(attempts.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): LenientServiceAreaWindow {
        return LenientServiceAreaWindow(
            code = code,
            title = title,
            externalRef = externalRef,
            stopCount = stopCount,
            endsAt = endsAt,
            amount = amount,
            expiresAt = expiresAt,
            checksum = checksum,
            attempts = attempts,
            country = country,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient service area window in status created. */
fun newLenientServiceAreaWindow(code: String, title: String): LenientServiceAreaWindow {
    return LenientServiceAreaWindow(
        code = code,
        title = title,
        externalRef = "",
        stopCount = 0,
        endsAt = 0,
        amount = 0.0,
        expiresAt = 0,
        checksum = "",
        attempts = 0,
        country = "",
        sequence = 0,
        status = LenientServiceAreaWindowStatus.CREATED,
    )
}
