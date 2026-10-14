package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge window is in its life cycle. */
enum class EvChargeWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a ev charge window and its current status. */
data class EvChargeWindow(
    var code: String,
    var label: String,
    var floorValue: Double,
    var graceDays: Int,
    var weight: Double,
    var phone: String,
    var ceiling: Double,
    var contactEmail: String,
    var cost: Double,
    var reviewer: String,
    var status: EvChargeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeWindowStatus): Boolean {
        when (status) {
            EvChargeWindowStatus.NEW -> {
                when (next) {
                    EvChargeWindowStatus.RUNNING, EvChargeWindowStatus.DONE -> return true
                }
                return false
            }
            EvChargeWindowStatus.QUEUED -> {
                when (next) {
                    EvChargeWindowStatus.FAILED -> return true
                }
                return false
            }
            EvChargeWindowStatus.RUNNING -> return next == EvChargeWindowStatus.FAILED
            EvChargeWindowStatus.DONE -> {
                when (next) {
                    EvChargeWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeWindowStatus) {
        if (!canMoveTo(next)) {
            error("ev charge window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round((weight - floorValue) * 100) / 100

    fun snapshot(): EvChargeWindow {
        return EvChargeWindow(
            code = code,
            label = label,
            floorValue = floorValue,
            graceDays = graceDays,
            weight = weight,
            phone = phone,
            ceiling = ceiling,
            contactEmail = contactEmail,
            cost = cost,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a ev charge window in status new. */
fun newEvChargeWindow(code: String, label: String): EvChargeWindow {
    return EvChargeWindow(
        code = code,
        label = label,
        floorValue = 0.0,
        graceDays = 0,
        weight = 0.0,
        phone = "",
        ceiling = 0.0,
        contactEmail = "",
        cost = 0.0,
        reviewer = "",
        status = EvChargeWindowStatus.NEW,
    )
}
