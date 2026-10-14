package com.acme.transport.safety.nearmiss.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss quota is in its life cycle. */
enum class NearMissQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a near miss quota and its current status. */
data class NearMissQuota(
    var code: String,
    var label: String,
    var batchSize: Int,
    var maximum: Double,
    var phone: String,
    var threshold: Double,
    var dock: String,
    var ratio: Double,
    var status: NearMissQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissQuotaStatus): Boolean {
        when (status) {
            NearMissQuotaStatus.IDLE -> {
                when (next) {
                    NearMissQuotaStatus.DRAINING -> return true
                }
                return false
            }
            NearMissQuotaStatus.WARMING -> {
                when (next) {
                    NearMissQuotaStatus.DRAINING, NearMissQuotaStatus.STOPPED -> return true
                }
                return false
            }
            NearMissQuotaStatus.READY -> {
                when (next) {
                    NearMissQuotaStatus.DRAINING, NearMissQuotaStatus.STOPPED -> return true
                }
                return false
            }
            NearMissQuotaStatus.DRAINING -> return next == NearMissQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: NearMissQuotaStatus) {
        if (!canMoveTo(next)) {
            error("near miss quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((maximum + threshold) * 100) / 100

    fun snapshot(): NearMissQuota {
        return NearMissQuota(
            code = code,
            label = label,
            batchSize = batchSize,
            maximum = maximum,
            phone = phone,
            threshold = threshold,
            dock = dock,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a near miss quota in status idle. */
fun newNearMissQuota(code: String, label: String): NearMissQuota {
    return NearMissQuota(
        code = code,
        label = label,
        batchSize = 0,
        maximum = 0.0,
        phone = "",
        threshold = 0.0,
        dock = "",
        ratio = 0.0,
        status = NearMissQuotaStatus.IDLE,
    )
}
