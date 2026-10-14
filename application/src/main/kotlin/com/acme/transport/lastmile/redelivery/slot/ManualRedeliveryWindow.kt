package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual redelivery window is in its life cycle. */
enum class ManualRedeliveryWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual redelivery window, with what the planners need to act on it. */
data class ManualRedeliveryWindow(
    var code: String,
    var name: String,
    var minimum: Double,
    var version: Int,
    var category: String,
    var etaMinutes: Int,
    var capacity: Int,
    var region: String,
    var threshold: Double,
    var status: ManualRedeliveryWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRedeliveryWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRedeliveryWindowStatus): Boolean {
        when (status) {
            ManualRedeliveryWindowStatus.CREATED -> {
                when (next) {
                    ManualRedeliveryWindowStatus.VALIDATED, ManualRedeliveryWindowStatus.RETIRED -> return true
                }
                return false
            }
            ManualRedeliveryWindowStatus.VALIDATED -> {
                when (next) {
                    ManualRedeliveryWindowStatus.RETIRED -> return true
                }
                return false
            }
            ManualRedeliveryWindowStatus.PUBLISHED -> return next == ManualRedeliveryWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualRedeliveryWindowStatus) {
        if (!canMoveTo(next)) {
            error("manual redelivery window $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round(version.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): ManualRedeliveryWindow {
        return ManualRedeliveryWindow(
            code = code,
            name = name,
            minimum = minimum,
            version = version,
            category = category,
            etaMinutes = etaMinutes,
            capacity = capacity,
            region = region,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual redelivery window in status created. */
fun newManualRedeliveryWindow(code: String, name: String): ManualRedeliveryWindow {
    return ManualRedeliveryWindow(
        code = code,
        name = name,
        minimum = 0.0,
        version = 0,
        category = "",
        etaMinutes = 0,
        capacity = 0,
        region = "",
        threshold = 0.0,
        status = ManualRedeliveryWindowStatus.CREATED,
    )
}
