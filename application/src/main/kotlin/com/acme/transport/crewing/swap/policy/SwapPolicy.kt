package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap policy records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap policy is in its life cycle. */
enum class SwapPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A swap policy. Its code is unique within a tenant. */
data class SwapPolicy(
    var code: String,
    var label: String,
    var summary: String,
    var floorValue: Double,
    var capacity: Int,
    var startsAt: Int,
    var amount: Double,
    var ratio: Double,
    var retries: Int,
    var cost: Double,
    var weight: Double,
    var status: SwapPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapPolicyStatus): Boolean {
        when (status) {
            SwapPolicyStatus.CREATED -> {
                when (next) {
                    SwapPolicyStatus.VALIDATED, SwapPolicyStatus.RETIRED -> return true
                }
                return false
            }
            SwapPolicyStatus.VALIDATED -> return next == SwapPolicyStatus.PUBLISHED
            SwapPolicyStatus.PUBLISHED -> {
                when (next) {
                    SwapPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapPolicyStatus) {
        if (!canMoveTo(next)) {
            error("swap policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"swap policy"} $code"

    fun projectedVolume(): Double = round(floorValue * cost * 100) / 100

    fun snapshot(): SwapPolicy {
        return SwapPolicy(
            code = code,
            label = label,
            summary = summary,
            floorValue = floorValue,
            capacity = capacity,
            startsAt = startsAt,
            amount = amount,
            ratio = ratio,
            retries = retries,
            cost = cost,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a swap policy in status created. */
fun newSwapPolicy(code: String, label: String): SwapPolicy {
    return SwapPolicy(
        code = code,
        label = label,
        summary = "",
        floorValue = 0.0,
        capacity = 0,
        startsAt = 0,
        amount = 0.0,
        ratio = 0.0,
        retries = 0,
        cost = 0.0,
        weight = 0.0,
        status = SwapPolicyStatus.CREATED,
    )
}
