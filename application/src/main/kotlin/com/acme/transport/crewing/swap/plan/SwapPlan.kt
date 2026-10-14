package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap plan records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap plan is in its life cycle. */
enum class SwapPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A swap plan. Its code is unique within a tenant. */
data class SwapPlan(
    var code: String,
    var title: String,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var contactEmail: String,
    var notes: String,
    var lane: String,
    var region: String,
    var currency: String,
    var quantity: Int,
    var status: SwapPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapPlanStatus): Boolean {
        when (status) {
            SwapPlanStatus.NEW -> {
                when (next) {
                    SwapPlanStatus.FAILED -> return true
                }
                return false
            }
            SwapPlanStatus.QUEUED -> return next == SwapPlanStatus.FAILED
            SwapPlanStatus.RUNNING -> {
                when (next) {
                    SwapPlanStatus.DONE, SwapPlanStatus.FAILED -> return true
                }
                return false
            }
            SwapPlanStatus.DONE -> return next == SwapPlanStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SwapPlanStatus) {
        if (!canMoveTo(next)) {
            error("swap plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"swap plan"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((quantity.toDouble() - margin) * 100) / 100

    fun snapshot(): SwapPlan {
        return SwapPlan(
            code = code,
            title = title,
            margin = margin,
            attributes = attributes,
            phone = phone,
            contactEmail = contactEmail,
            notes = notes,
            lane = lane,
            region = region,
            currency = currency,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a swap plan in status new. */
fun newSwapPlan(code: String, title: String): SwapPlan {
    return SwapPlan(
        code = code,
        title = title,
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        phone = "",
        contactEmail = "",
        notes = "",
        lane = "",
        region = "",
        currency = "",
        quantity = 0,
        status = SwapPlanStatus.NEW,
    )
}
