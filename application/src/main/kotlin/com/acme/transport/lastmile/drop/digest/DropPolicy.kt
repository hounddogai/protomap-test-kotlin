package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop policy is in its life cycle. */
enum class DropPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A drop policy. Its code is unique within a tenant. */
data class DropPolicy(
    var code: String,
    var title: String,
    var ceiling: Double,
    var source: String,
    var priority: Int,
    var capacity: Int,
    var name: String,
    var label: String,
    var trailerId: String,
    var status: DropPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropPolicyStatus): Boolean {
        when (status) {
            DropPolicyStatus.REQUESTED -> {
                when (next) {
                    DropPolicyStatus.GRANTED -> return true
                }
                return false
            }
            DropPolicyStatus.GRANTED -> return next == DropPolicyStatus.REVOKED || next == DropPolicyStatus.EXPIRED
            DropPolicyStatus.REVOKED -> return next == DropPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DropPolicyStatus) {
        if (!canMoveTo(next)) {
            error("drop policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"drop policy"} $code"

    fun netValue(): Double = round(capacity.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): DropPolicy {
        return DropPolicy(
            code = code,
            title = title,
            ceiling = ceiling,
            source = source,
            priority = priority,
            capacity = capacity,
            name = name,
            label = label,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a drop policy in status requested. */
fun newDropPolicy(code: String, title: String): DropPolicy {
    return DropPolicy(
        code = code,
        title = title,
        ceiling = 0.0,
        source = "",
        priority = 0,
        capacity = 0,
        name = "",
        label = "",
        trailerId = "",
        status = DropPolicyStatus.REQUESTED,
    )
}
