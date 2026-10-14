package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker request is in its life cycle. */
enum class BrokerRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a broker request and its current status. */
data class BrokerRequest(
    var code: String,
    var label: String,
    var odometer: Int,
    var revision: Int,
    var pinned: Boolean,
    var score: Double,
    var floorValue: Double,
    var status: BrokerRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerRequestStatus): Boolean {
        when (status) {
            BrokerRequestStatus.PLANNED -> {
                when (next) {
                    BrokerRequestStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerRequestStatus.SCHEDULED -> return next == BrokerRequestStatus.CANCELLED
            BrokerRequestStatus.IN_PROGRESS -> {
                when (next) {
                    BrokerRequestStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerRequestStatus.COMPLETED -> return next == BrokerRequestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BrokerRequestStatus) {
        if (!canMoveTo(next)) {
            error("broker request $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round((score + revision.toDouble()) * 100) / 100

    fun snapshot(): BrokerRequest {
        return BrokerRequest(
            code = code,
            label = label,
            odometer = odometer,
            revision = revision,
            pinned = pinned,
            score = score,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a broker request in status planned. */
fun newBrokerRequest(code: String, label: String): BrokerRequest {
    return BrokerRequest(
        code = code,
        label = label,
        odometer = 0,
        revision = 0,
        pinned = false,
        score = 0.0,
        floorValue = 0.0,
        status = BrokerRequestStatus.PLANNED,
    )
}
