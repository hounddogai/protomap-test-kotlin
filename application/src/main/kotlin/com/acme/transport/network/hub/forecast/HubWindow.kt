package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub window is in its life cycle. */
enum class HubWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A hub window. Its code is unique within a tenant. */
data class HubWindow(
    var code: String,
    var name: String,
    var description: String,
    var updatedAt: Int,
    var shiftCode: String,
    var odometer: Int,
    var archived: Boolean,
    var status: HubWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubWindowStatus): Boolean {
        when (status) {
            HubWindowStatus.NEW -> return next == HubWindowStatus.RUNNING
            HubWindowStatus.QUEUED -> {
                when (next) {
                    HubWindowStatus.DONE, HubWindowStatus.FAILED -> return true
                }
                return false
            }
            HubWindowStatus.RUNNING -> {
                when (next) {
                    HubWindowStatus.DONE -> return true
                }
                return false
            }
            HubWindowStatus.DONE -> {
                when (next) {
                    HubWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubWindowStatus) {
        if (!canMoveTo(next)) {
            error("hub window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((odometer.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): HubWindow {
        return HubWindow(
            code = code,
            name = name,
            description = description,
            updatedAt = updatedAt,
            shiftCode = shiftCode,
            odometer = odometer,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hub window in status new. */
fun newHubWindow(code: String, name: String): HubWindow {
    return HubWindow(
        code = code,
        name = name,
        description = "",
        updatedAt = 0,
        shiftCode = "",
        odometer = 0,
        archived = false,
        status = HubWindowStatus.NEW,
    )
}
