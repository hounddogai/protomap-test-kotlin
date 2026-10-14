package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.text.normalize

// The manual dispute bundle records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a manual dispute bundle is in its life cycle. */
enum class ManualDisputeBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual dispute bundle, with what the planners need to act on it. */
data class ManualDisputeBundle(
    var code: String,
    var label: String,
    var region: String,
    var notes: String,
    var summary: String,
    var amount: Double,
    var status: ManualDisputeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDisputeBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDisputeBundleStatus): Boolean {
        when (status) {
            ManualDisputeBundleStatus.OPEN -> {
                when (next) {
                    ManualDisputeBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualDisputeBundleStatus.IN_REVIEW -> {
                when (next) {
                    ManualDisputeBundleStatus.RESOLVED, ManualDisputeBundleStatus.REOPENED -> return true
                }
                return false
            }
            ManualDisputeBundleStatus.RESOLVED -> {
                when (next) {
                    ManualDisputeBundleStatus.REOPENED, ManualDisputeBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualDisputeBundleStatus.REOPENED -> {
                when (next) {
                    ManualDisputeBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDisputeBundleStatus) {
        if (!canMoveTo(next)) {
            error("manual dispute bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = amount

    fun snapshot(): ManualDisputeBundle {
        return ManualDisputeBundle(
            code = code,
            label = label,
            region = region,
            notes = notes,
            summary = summary,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual dispute bundle in status open. */
fun newManualDisputeBundle(code: String, label: String): ManualDisputeBundle {
    return ManualDisputeBundle(
        code = code,
        label = label,
        region = "",
        notes = "",
        summary = "",
        amount = 0.0,
        status = ManualDisputeBundleStatus.OPEN,
    )
}
