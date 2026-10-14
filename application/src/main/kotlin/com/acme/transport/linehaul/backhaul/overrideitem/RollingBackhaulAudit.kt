package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling backhaul audit is in its life cycle. */
enum class RollingBackhaulAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling backhaul audit. Its code is unique within a tenant. */
data class RollingBackhaulAudit(
    var code: String,
    var label: String,
    var region: String,
    var lane: String,
    var visible: Boolean,
    var maximum: Double,
    var discount: Double,
    var status: RollingBackhaulAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBackhaulAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBackhaulAuditStatus): Boolean {
        when (status) {
            RollingBackhaulAuditStatus.NEW -> {
                when (next) {
                    RollingBackhaulAuditStatus.RUNNING, RollingBackhaulAuditStatus.FAILED -> return true
                }
                return false
            }
            RollingBackhaulAuditStatus.QUEUED -> {
                when (next) {
                    RollingBackhaulAuditStatus.RUNNING -> return true
                }
                return false
            }
            RollingBackhaulAuditStatus.RUNNING -> {
                when (next) {
                    RollingBackhaulAuditStatus.DONE, RollingBackhaulAuditStatus.FAILED -> return true
                }
                return false
            }
            RollingBackhaulAuditStatus.DONE -> return next == RollingBackhaulAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingBackhaulAuditStatus) {
        if (!canMoveTo(next)) {
            error("rolling backhaul audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling backhaul audit"} $code"

    fun effectiveVolume(): Double = round((maximum + discount) * 100) / 100

    fun snapshot(): RollingBackhaulAudit {
        return RollingBackhaulAudit(
            code = code,
            label = label,
            region = region,
            lane = lane,
            visible = visible,
            maximum = maximum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling backhaul audit in status new. */
fun newRollingBackhaulAudit(code: String, label: String): RollingBackhaulAudit {
    return RollingBackhaulAudit(
        code = code,
        label = label,
        region = "",
        lane = "",
        visible = false,
        maximum = 0.0,
        discount = 0.0,
        status = RollingBackhaulAuditStatus.NEW,
    )
}
