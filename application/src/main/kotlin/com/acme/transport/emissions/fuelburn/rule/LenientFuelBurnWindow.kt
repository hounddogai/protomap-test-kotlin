package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient fuel burn window is in its life cycle. */
enum class LenientFuelBurnWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient fuel burn window as the fuel burn team keeps it. */
data class LenientFuelBurnWindow(
    var code: String,
    var title: String,
    var retries: Int,
    var ownerId: String,
    var updatedAt: Int,
    var budget: Double,
    var status: LenientFuelBurnWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelBurnWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelBurnWindowStatus): Boolean {
        when (status) {
            LenientFuelBurnWindowStatus.NEW -> {
                when (next) {
                    LenientFuelBurnWindowStatus.FAILED -> return true
                }
                return false
            }
            LenientFuelBurnWindowStatus.QUEUED -> return next == LenientFuelBurnWindowStatus.RUNNING || next == LenientFuelBurnWindowStatus.DONE
            LenientFuelBurnWindowStatus.RUNNING -> return next == LenientFuelBurnWindowStatus.DONE
            LenientFuelBurnWindowStatus.DONE -> return next == LenientFuelBurnWindowStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientFuelBurnWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel burn window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((budget - retries.toDouble()) * 100) / 100

    fun snapshot(): LenientFuelBurnWindow {
        return LenientFuelBurnWindow(
            code = code,
            title = title,
            retries = retries,
            ownerId = ownerId,
            updatedAt = updatedAt,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient fuel burn window in status new. */
fun newLenientFuelBurnWindow(code: String, title: String): LenientFuelBurnWindow {
    return LenientFuelBurnWindow(
        code = code,
        title = title,
        retries = 0,
        ownerId = "",
        updatedAt = 0,
        budget = 0.0,
        status = LenientFuelBurnWindowStatus.NEW,
    )
}
