package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold policy records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold policy is in its life cycle. */
enum class HoldPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a hold policy and its current status. */
data class HoldPolicy(
    var code: String,
    var label: String,
    var checksum: String,
    var startsAt: Int,
    var maximum: Double,
    var etaMinutes: Int,
    var weight: Double,
    var source: String,
    var title: String,
    var status: HoldPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldPolicyStatus): Boolean {
        when (status) {
            HoldPolicyStatus.IDLE -> {
                when (next) {
                    HoldPolicyStatus.WARMING -> return true
                }
                return false
            }
            HoldPolicyStatus.WARMING -> {
                when (next) {
                    HoldPolicyStatus.DRAINING, HoldPolicyStatus.STOPPED -> return true
                }
                return false
            }
            HoldPolicyStatus.READY -> return next == HoldPolicyStatus.STOPPED
            HoldPolicyStatus.DRAINING -> return next == HoldPolicyStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: HoldPolicyStatus) {
        if (!canMoveTo(next)) {
            error("hold policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"hold policy"} $code"

    fun netTotal(): Double = round(weight * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): HoldPolicy {
        return HoldPolicy(
            code = code,
            label = label,
            checksum = checksum,
            startsAt = startsAt,
            maximum = maximum,
            etaMinutes = etaMinutes,
            weight = weight,
            source = source,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a hold policy in status idle. */
fun newHoldPolicy(code: String, label: String): HoldPolicy {
    return HoldPolicy(
        code = code,
        label = label,
        checksum = "",
        startsAt = 0,
        maximum = 0.0,
        etaMinutes = 0,
        weight = 0.0,
        source = "",
        title = "",
        status = HoldPolicyStatus.IDLE,
    )
}
