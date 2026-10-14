package com.acme.transport.network.hub.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub notice records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub notice is in its life cycle. */
enum class HubNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One hub notice, with what the planners need to act on it. */
data class HubNotice(
    var code: String,
    var name: String,
    var source: String,
    var attempts: Int,
    var trailerId: String,
    var amount: Double,
    var createdAt: Int,
    var fuelLiters: Double,
    var maximum: Double,
    var odometer: Int,
    var shiftCode: String,
    var status: HubNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubNoticeStatus): Boolean {
        when (status) {
            HubNoticeStatus.NEW -> {
                when (next) {
                    HubNoticeStatus.DONE -> return true
                }
                return false
            }
            HubNoticeStatus.QUEUED -> return next == HubNoticeStatus.FAILED
            HubNoticeStatus.RUNNING -> {
                when (next) {
                    HubNoticeStatus.DONE -> return true
                }
                return false
            }
            HubNoticeStatus.DONE -> return next == HubNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: HubNoticeStatus) {
        if (!canMoveTo(next)) {
            error("hub notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((odometer.toDouble() + amount) * 100) / 100

    fun snapshot(): HubNotice {
        return HubNotice(
            code = code,
            name = name,
            source = source,
            attempts = attempts,
            trailerId = trailerId,
            amount = amount,
            createdAt = createdAt,
            fuelLiters = fuelLiters,
            maximum = maximum,
            odometer = odometer,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a hub notice in status new. */
fun newHubNotice(code: String, name: String): HubNotice {
    return HubNotice(
        code = code,
        name = name,
        source = "",
        attempts = 0,
        trailerId = "",
        amount = 0.0,
        createdAt = 0,
        fuelLiters = 0.0,
        maximum = 0.0,
        odometer = 0,
        shiftCode = "",
        status = HubNoticeStatus.NEW,
    )
}
