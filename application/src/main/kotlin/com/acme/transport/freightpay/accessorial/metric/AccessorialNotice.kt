package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial notice is in its life cycle. */
enum class AccessorialNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A accessorial notice. Its code is unique within a tenant. */
data class AccessorialNotice(
    var code: String,
    var label: String,
    var routeCode: String,
    var segment: String,
    var ceiling: Double,
    var budget: Double,
    var maximum: Double,
    var ownerId: String,
    var batchSize: Int,
    var name: String,
    var status: AccessorialNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialNoticeStatus): Boolean {
        when (status) {
            AccessorialNoticeStatus.REQUESTED -> {
                when (next) {
                    AccessorialNoticeStatus.REVOKED -> return true
                }
                return false
            }
            AccessorialNoticeStatus.GRANTED -> return next == AccessorialNoticeStatus.REVOKED || next == AccessorialNoticeStatus.EXPIRED
            AccessorialNoticeStatus.REVOKED -> return next == AccessorialNoticeStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AccessorialNoticeStatus) {
        if (!canMoveTo(next)) {
            error("accessorial notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"accessorial notice"} $code"

    fun projectedVolume(): Double = round((ceiling - maximum) * 100) / 100

    fun snapshot(): AccessorialNotice {
        return AccessorialNotice(
            code = code,
            label = label,
            routeCode = routeCode,
            segment = segment,
            ceiling = ceiling,
            budget = budget,
            maximum = maximum,
            ownerId = ownerId,
            batchSize = batchSize,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a accessorial notice in status requested. */
fun newAccessorialNotice(code: String, label: String): AccessorialNotice {
    return AccessorialNotice(
        code = code,
        label = label,
        routeCode = "",
        segment = "",
        ceiling = 0.0,
        budget = 0.0,
        maximum = 0.0,
        ownerId = "",
        batchSize = 0,
        name = "",
        status = AccessorialNoticeStatus.REQUESTED,
    )
}
