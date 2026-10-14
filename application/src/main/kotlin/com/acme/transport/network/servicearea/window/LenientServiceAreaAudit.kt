package com.acme.transport.network.servicearea.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient service area audit is in its life cycle. */
enum class LenientServiceAreaAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lenient service area audit and its current status. */
data class LenientServiceAreaAudit(
    var code: String,
    var title: String,
    var visible: Boolean,
    var depot: String,
    var stopCount: Int,
    var channel: String,
    var cost: Double,
    var status: LenientServiceAreaAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServiceAreaAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServiceAreaAuditStatus): Boolean {
        when (status) {
            LenientServiceAreaAuditStatus.REQUESTED -> {
                when (next) {
                    LenientServiceAreaAuditStatus.GRANTED, LenientServiceAreaAuditStatus.REVOKED -> return true
                }
                return false
            }
            LenientServiceAreaAuditStatus.GRANTED -> {
                when (next) {
                    LenientServiceAreaAuditStatus.REVOKED -> return true
                }
                return false
            }
            LenientServiceAreaAuditStatus.REVOKED -> {
                when (next) {
                    LenientServiceAreaAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientServiceAreaAuditStatus) {
        if (!canMoveTo(next)) {
            error("lenient service area audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient service area audit"} $code"

    fun weightedVolume(): Double = round((cost - stopCount.toDouble()) * 100) / 100

    fun snapshot(): LenientServiceAreaAudit {
        return LenientServiceAreaAudit(
            code = code,
            title = title,
            visible = visible,
            depot = depot,
            stopCount = stopCount,
            channel = channel,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient service area audit in status requested. */
fun newLenientServiceAreaAudit(code: String, title: String): LenientServiceAreaAudit {
    return LenientServiceAreaAudit(
        code = code,
        title = title,
        visible = false,
        depot = "",
        stopCount = 0,
        channel = "",
        cost = 0.0,
        status = LenientServiceAreaAuditStatus.REQUESTED,
    )
}
