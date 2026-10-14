package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual dispute request records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a manual dispute request is in its life cycle. */
enum class ManualDisputeRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual dispute request and its current status. */
data class ManualDisputeRequest(
    var code: String,
    var label: String,
    var contactEmail: String,
    var graceDays: Int,
    var approved: Boolean,
    var discount: Double,
    var batchSize: Int,
    var weight: Double,
    var active: Boolean,
    var driverId: String,
    var status: ManualDisputeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDisputeRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDisputeRequestStatus): Boolean {
        when (status) {
            ManualDisputeRequestStatus.NEW -> return next == ManualDisputeRequestStatus.QUEUED || next == ManualDisputeRequestStatus.FAILED
            ManualDisputeRequestStatus.QUEUED -> {
                when (next) {
                    ManualDisputeRequestStatus.DONE, ManualDisputeRequestStatus.FAILED -> return true
                }
                return false
            }
            ManualDisputeRequestStatus.RUNNING -> return next == ManualDisputeRequestStatus.FAILED
            ManualDisputeRequestStatus.DONE -> return next == ManualDisputeRequestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualDisputeRequestStatus) {
        if (!canMoveTo(next)) {
            error("manual dispute request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round((discount + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ManualDisputeRequest {
        return ManualDisputeRequest(
            code = code,
            label = label,
            contactEmail = contactEmail,
            graceDays = graceDays,
            approved = approved,
            discount = discount,
            batchSize = batchSize,
            weight = weight,
            active = active,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual dispute request in status new. */
fun newManualDisputeRequest(code: String, label: String): ManualDisputeRequest {
    return ManualDisputeRequest(
        code = code,
        label = label,
        contactEmail = "",
        graceDays = 0,
        approved = false,
        discount = 0.0,
        batchSize = 0,
        weight = 0.0,
        active = false,
        driverId = "",
        status = ManualDisputeRequestStatus.NEW,
    )
}
