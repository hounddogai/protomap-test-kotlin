package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.text.normalize

// The sortation policy records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation policy is in its life cycle. */
enum class SortationPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A sortation policy. Its code is unique within a tenant. */
data class SortationPolicy(
    var code: String,
    var name: String,
    var checksum: String,
    var visible: Boolean,
    var routeCode: String,
    var notes: String,
    var score: Double,
    var status: SortationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationPolicyStatus): Boolean {
        when (status) {
            SortationPolicyStatus.IDLE -> {
                when (next) {
                    SortationPolicyStatus.WARMING, SortationPolicyStatus.READY -> return true
                }
                return false
            }
            SortationPolicyStatus.WARMING -> {
                when (next) {
                    SortationPolicyStatus.READY, SortationPolicyStatus.STOPPED -> return true
                }
                return false
            }
            SortationPolicyStatus.READY -> return next == SortationPolicyStatus.DRAINING || next == SortationPolicyStatus.STOPPED
            SortationPolicyStatus.DRAINING -> {
                when (next) {
                    SortationPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("sortation policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = score

    fun snapshot(): SortationPolicy {
        return SortationPolicy(
            code = code,
            name = name,
            checksum = checksum,
            visible = visible,
            routeCode = routeCode,
            notes = notes,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a sortation policy in status idle. */
fun newSortationPolicy(code: String, name: String): SortationPolicy {
    return SortationPolicy(
        code = code,
        name = name,
        checksum = "",
        visible = false,
        routeCode = "",
        notes = "",
        score = 0.0,
        status = SortationPolicyStatus.IDLE,
    )
}
