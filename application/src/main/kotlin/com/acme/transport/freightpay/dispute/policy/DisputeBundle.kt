package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute bundle is in its life cycle. */
enum class DisputeBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dispute bundle as the dispute team keeps it. */
data class DisputeBundle(
    var code: String,
    var title: String,
    var windowDays: Int,
    var graceDays: Int,
    var checksum: String,
    var contactEmail: String,
    var externalRef: String,
    var active: Boolean,
    var pallets: Int,
    var status: DisputeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeBundleStatus): Boolean {
        when (status) {
            DisputeBundleStatus.NEW -> {
                when (next) {
                    DisputeBundleStatus.QUEUED, DisputeBundleStatus.FAILED -> return true
                }
                return false
            }
            DisputeBundleStatus.QUEUED -> return next == DisputeBundleStatus.RUNNING
            DisputeBundleStatus.RUNNING -> return next == DisputeBundleStatus.DONE || next == DisputeBundleStatus.FAILED
            DisputeBundleStatus.DONE -> {
                when (next) {
                    DisputeBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeBundleStatus) {
        if (!canMoveTo(next)) {
            error("dispute bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round((graceDays.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): DisputeBundle {
        return DisputeBundle(
            code = code,
            title = title,
            windowDays = windowDays,
            graceDays = graceDays,
            checksum = checksum,
            contactEmail = contactEmail,
            externalRef = externalRef,
            active = active,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dispute bundle in status new. */
fun newDisputeBundle(code: String, title: String): DisputeBundle {
    return DisputeBundle(
        code = code,
        title = title,
        windowDays = 0,
        graceDays = 0,
        checksum = "",
        contactEmail = "",
        externalRef = "",
        active = false,
        pallets = 0,
        status = DisputeBundleStatus.NEW,
    )
}
