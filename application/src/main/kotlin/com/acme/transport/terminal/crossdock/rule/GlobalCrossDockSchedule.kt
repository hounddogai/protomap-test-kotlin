package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global cross dock schedule is in its life cycle. */
enum class GlobalCrossDockScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global cross dock schedule. Its code is unique within a tenant. */
data class GlobalCrossDockSchedule(
    var code: String,
    var title: String,
    var pallets: Int,
    var country: String,
    var loadKg: Double,
    var name: String,
    var currency: String,
    var region: String,
    var graceDays: Int,
    var label: String,
    var amount: Double,
    var status: GlobalCrossDockScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCrossDockScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCrossDockScheduleStatus): Boolean {
        when (status) {
            GlobalCrossDockScheduleStatus.PROPOSED -> return next == GlobalCrossDockScheduleStatus.APPROVED || next == GlobalCrossDockScheduleStatus.WITHDRAWN
            GlobalCrossDockScheduleStatus.APPROVED -> {
                when (next) {
                    GlobalCrossDockScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalCrossDockScheduleStatus.REJECTED -> return next == GlobalCrossDockScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalCrossDockScheduleStatus) {
        if (!canMoveTo(next)) {
            error("global cross dock schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global cross dock schedule"} $code"

    fun weightedVolume(): Double = round((pallets.toDouble() + amount) * 100) / 100

    fun snapshot(): GlobalCrossDockSchedule {
        return GlobalCrossDockSchedule(
            code = code,
            title = title,
            pallets = pallets,
            country = country,
            loadKg = loadKg,
            name = name,
            currency = currency,
            region = region,
            graceDays = graceDays,
            label = label,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global cross dock schedule in status proposed. */
fun newGlobalCrossDockSchedule(code: String, title: String): GlobalCrossDockSchedule {
    return GlobalCrossDockSchedule(
        code = code,
        title = title,
        pallets = 0,
        country = "",
        loadKg = 0.0,
        name = "",
        currency = "",
        region = "",
        graceDays = 0,
        label = "",
        amount = 0.0,
        status = GlobalCrossDockScheduleStatus.PROPOSED,
    )
}
