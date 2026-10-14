package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global redelivery override is in its life cycle. */
enum class GlobalRedeliveryOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global redelivery override. Its code is unique within a tenant. */
data class GlobalRedeliveryOverride(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var reviewer: String,
    var discount: Double,
    var depot: String,
    var margin: Double,
    var channel: String,
    var threshold: Double,
    var timezone: String,
    var visible: Boolean,
    var status: GlobalRedeliveryOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRedeliveryOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRedeliveryOverrideStatus): Boolean {
        when (status) {
            GlobalRedeliveryOverrideStatus.NEW -> return next == GlobalRedeliveryOverrideStatus.RUNNING
            GlobalRedeliveryOverrideStatus.QUEUED -> {
                when (next) {
                    GlobalRedeliveryOverrideStatus.RUNNING, GlobalRedeliveryOverrideStatus.DONE -> return true
                }
                return false
            }
            GlobalRedeliveryOverrideStatus.RUNNING -> return next == GlobalRedeliveryOverrideStatus.DONE || next == GlobalRedeliveryOverrideStatus.FAILED
            GlobalRedeliveryOverrideStatus.DONE -> return next == GlobalRedeliveryOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalRedeliveryOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global redelivery override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global redelivery override"} $code"

    fun projectedValue(): Double = round(margin * threshold * 100) / 100

    fun snapshot(): GlobalRedeliveryOverride {
        return GlobalRedeliveryOverride(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            reviewer = reviewer,
            discount = discount,
            depot = depot,
            margin = margin,
            channel = channel,
            threshold = threshold,
            timezone = timezone,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global redelivery override in status new. */
fun newGlobalRedeliveryOverride(code: String, label: String): GlobalRedeliveryOverride {
    return GlobalRedeliveryOverride(
        code = code,
        label = label,
        durationMinutes = 0,
        reviewer = "",
        discount = 0.0,
        depot = "",
        margin = 0.0,
        channel = "",
        threshold = 0.0,
        timezone = "",
        visible = false,
        status = GlobalRedeliveryOverrideStatus.NEW,
    )
}
