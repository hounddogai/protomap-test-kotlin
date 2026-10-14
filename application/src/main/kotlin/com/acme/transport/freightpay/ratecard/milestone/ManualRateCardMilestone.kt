package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual rate card milestone records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a manual rate card milestone is in its life cycle. */
enum class ManualRateCardMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual rate card milestone and its current status. */
data class ManualRateCardMilestone(
    var code: String,
    var label: String,
    var amount: Double,
    var floorValue: Double,
    var depot: String,
    var quantity: Int,
    var checksum: String,
    var status: ManualRateCardMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRateCardMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRateCardMilestoneStatus): Boolean {
        when (status) {
            ManualRateCardMilestoneStatus.NEW -> return next == ManualRateCardMilestoneStatus.QUEUED || next == ManualRateCardMilestoneStatus.RUNNING
            ManualRateCardMilestoneStatus.QUEUED -> {
                when (next) {
                    ManualRateCardMilestoneStatus.DONE -> return true
                }
                return false
            }
            ManualRateCardMilestoneStatus.RUNNING -> return next == ManualRateCardMilestoneStatus.FAILED
            ManualRateCardMilestoneStatus.DONE -> return next == ManualRateCardMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualRateCardMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("manual rate card milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveLoad(): Double = round(quantity.toDouble() * amount * 100) / 100

    fun snapshot(): ManualRateCardMilestone {
        return ManualRateCardMilestone(
            code = code,
            label = label,
            amount = amount,
            floorValue = floorValue,
            depot = depot,
            quantity = quantity,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual rate card milestone in status new. */
fun newManualRateCardMilestone(code: String, label: String): ManualRateCardMilestone {
    return ManualRateCardMilestone(
        code = code,
        label = label,
        amount = 0.0,
        floorValue = 0.0,
        depot = "",
        quantity = 0,
        checksum = "",
        status = ManualRateCardMilestoneStatus.NEW,
    )
}
