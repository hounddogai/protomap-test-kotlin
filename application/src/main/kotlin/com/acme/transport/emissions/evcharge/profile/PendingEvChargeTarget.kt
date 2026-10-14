package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending ev charge target is in its life cycle. */
enum class PendingEvChargeTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending ev charge target. Its code is unique within a tenant. */
data class PendingEvChargeTarget(
    var code: String,
    var label: String,
    var category: String,
    var quantity: Int,
    var driverId: String,
    var currency: String,
    var limit: Int,
    var expiresAt: Int,
    var etaMinutes: Int,
    var revision: Int,
    var status: PendingEvChargeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingEvChargeTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingEvChargeTargetStatus): Boolean {
        when (status) {
            PendingEvChargeTargetStatus.NEW -> {
                when (next) {
                    PendingEvChargeTargetStatus.QUEUED, PendingEvChargeTargetStatus.DONE -> return true
                }
                return false
            }
            PendingEvChargeTargetStatus.QUEUED -> return next == PendingEvChargeTargetStatus.RUNNING || next == PendingEvChargeTargetStatus.DONE
            PendingEvChargeTargetStatus.RUNNING -> {
                when (next) {
                    PendingEvChargeTargetStatus.DONE, PendingEvChargeTargetStatus.FAILED -> return true
                }
                return false
            }
            PendingEvChargeTargetStatus.DONE -> {
                when (next) {
                    PendingEvChargeTargetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingEvChargeTargetStatus) {
        if (!canMoveTo(next)) {
            error("pending ev charge target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round((expiresAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingEvChargeTarget {
        return PendingEvChargeTarget(
            code = code,
            label = label,
            category = category,
            quantity = quantity,
            driverId = driverId,
            currency = currency,
            limit = limit,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending ev charge target in status new. */
fun newPendingEvChargeTarget(code: String, label: String): PendingEvChargeTarget {
    return PendingEvChargeTarget(
        code = code,
        label = label,
        category = "",
        quantity = 0,
        driverId = "",
        currency = "",
        limit = 0,
        expiresAt = 0,
        etaMinutes = 0,
        revision = 0,
        status = PendingEvChargeTargetStatus.NEW,
    )
}
