package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break window is in its life cycle. */
enum class RestBreakWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rest break window. Its code is unique within a tenant. */
data class RestBreakWindow(
    var code: String,
    var name: String,
    var ratio: Double,
    var expiresAt: Int,
    var carrier: String,
    var archived: Boolean,
    var currency: String,
    var channel: String,
    var status: RestBreakWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakWindowStatus): Boolean {
        when (status) {
            RestBreakWindowStatus.REQUESTED -> return next == RestBreakWindowStatus.GRANTED || next == RestBreakWindowStatus.EXPIRED
            RestBreakWindowStatus.GRANTED -> {
                when (next) {
                    RestBreakWindowStatus.EXPIRED -> return true
                }
                return false
            }
            RestBreakWindowStatus.REVOKED -> return next == RestBreakWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RestBreakWindowStatus) {
        if (!canMoveTo(next)) {
            error("rest break window $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((ratio + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakWindow {
        return RestBreakWindow(
            code = code,
            name = name,
            ratio = ratio,
            expiresAt = expiresAt,
            carrier = carrier,
            archived = archived,
            currency = currency,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rest break window in status requested. */
fun newRestBreakWindow(code: String, name: String): RestBreakWindow {
    return RestBreakWindow(
        code = code,
        name = name,
        ratio = 0.0,
        expiresAt = 0,
        carrier = "",
        archived = false,
        currency = "",
        channel = "",
        status = RestBreakWindowStatus.REQUESTED,
    )
}
