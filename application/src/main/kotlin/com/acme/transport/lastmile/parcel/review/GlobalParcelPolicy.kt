package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global parcel policy is in its life cycle. */
enum class GlobalParcelPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global parcel policy and its current status. */
data class GlobalParcelPolicy(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var lane: String,
    var margin: Double,
    var createdAt: Int,
    var priority: Int,
    var status: GlobalParcelPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalParcelPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalParcelPolicyStatus): Boolean {
        when (status) {
            GlobalParcelPolicyStatus.NEW -> {
                when (next) {
                    GlobalParcelPolicyStatus.FAILED -> return true
                }
                return false
            }
            GlobalParcelPolicyStatus.QUEUED -> return next == GlobalParcelPolicyStatus.RUNNING || next == GlobalParcelPolicyStatus.DONE
            GlobalParcelPolicyStatus.RUNNING -> return next == GlobalParcelPolicyStatus.FAILED
            GlobalParcelPolicyStatus.DONE -> {
                when (next) {
                    GlobalParcelPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalParcelPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global parcel policy $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((margin - createdAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalParcelPolicy {
        return GlobalParcelPolicy(
            code = code,
            label = label,
            expiresAt = expiresAt,
            lane = lane,
            margin = margin,
            createdAt = createdAt,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global parcel policy in status new. */
fun newGlobalParcelPolicy(code: String, label: String): GlobalParcelPolicy {
    return GlobalParcelPolicy(
        code = code,
        label = label,
        expiresAt = 0,
        lane = "",
        margin = 0.0,
        createdAt = 0,
        priority = 0,
        status = GlobalParcelPolicyStatus.NEW,
    )
}
