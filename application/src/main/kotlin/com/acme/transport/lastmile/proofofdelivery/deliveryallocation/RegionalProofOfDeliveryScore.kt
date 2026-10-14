package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional proof of delivery score is in its life cycle. */
enum class RegionalProofOfDeliveryScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional proof of delivery score. Its code is unique within a tenant. */
data class RegionalProofOfDeliveryScore(
    var code: String,
    var label: String,
    var dock: String,
    var capacity: Int,
    var sequence: Int,
    var attempts: Int,
    var status: RegionalProofOfDeliveryScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalProofOfDeliveryScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalProofOfDeliveryScoreStatus): Boolean {
        when (status) {
            RegionalProofOfDeliveryScoreStatus.NEW -> {
                when (next) {
                    RegionalProofOfDeliveryScoreStatus.QUEUED -> return true
                }
                return false
            }
            RegionalProofOfDeliveryScoreStatus.QUEUED -> {
                when (next) {
                    RegionalProofOfDeliveryScoreStatus.RUNNING, RegionalProofOfDeliveryScoreStatus.FAILED -> return true
                }
                return false
            }
            RegionalProofOfDeliveryScoreStatus.RUNNING -> return next == RegionalProofOfDeliveryScoreStatus.DONE || next == RegionalProofOfDeliveryScoreStatus.FAILED
            RegionalProofOfDeliveryScoreStatus.DONE -> {
                when (next) {
                    RegionalProofOfDeliveryScoreStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalProofOfDeliveryScoreStatus) {
        if (!canMoveTo(next)) {
            error("regional proof of delivery score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((attempts.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): RegionalProofOfDeliveryScore {
        return RegionalProofOfDeliveryScore(
            code = code,
            label = label,
            dock = dock,
            capacity = capacity,
            sequence = sequence,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional proof of delivery score in status new. */
fun newRegionalProofOfDeliveryScore(code: String, label: String): RegionalProofOfDeliveryScore {
    return RegionalProofOfDeliveryScore(
        code = code,
        label = label,
        dock = "",
        capacity = 0,
        sequence = 0,
        attempts = 0,
        status = RegionalProofOfDeliveryScoreStatus.NEW,
    )
}
