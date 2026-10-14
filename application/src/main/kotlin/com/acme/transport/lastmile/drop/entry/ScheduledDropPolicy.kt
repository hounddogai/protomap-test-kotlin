package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled drop policy is in its life cycle. */
enum class ScheduledDropPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scheduled drop policy and its current status. */
data class ScheduledDropPolicy(
    var code: String,
    var name: String,
    var createdAt: Int,
    var revision: Int,
    var rate: Double,
    var dock: String,
    var pallets: Int,
    var odometer: Int,
    var status: ScheduledDropPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDropPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDropPolicyStatus): Boolean {
        when (status) {
            ScheduledDropPolicyStatus.REQUESTED -> {
                when (next) {
                    ScheduledDropPolicyStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledDropPolicyStatus.GRANTED -> return next == ScheduledDropPolicyStatus.REVOKED || next == ScheduledDropPolicyStatus.EXPIRED
            ScheduledDropPolicyStatus.REVOKED -> {
                when (next) {
                    ScheduledDropPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDropPolicyStatus) {
        if (!canMoveTo(next)) {
            error("scheduled drop policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"scheduled drop policy"} $code"

    fun netValue(): Double = round((createdAt.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDropPolicy {
        return ScheduledDropPolicy(
            code = code,
            name = name,
            createdAt = createdAt,
            revision = revision,
            rate = rate,
            dock = dock,
            pallets = pallets,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled drop policy in status requested. */
fun newScheduledDropPolicy(code: String, name: String): ScheduledDropPolicy {
    return ScheduledDropPolicy(
        code = code,
        name = name,
        createdAt = 0,
        revision = 0,
        rate = 0.0,
        dock = "",
        pallets = 0,
        odometer = 0,
        status = ScheduledDropPolicyStatus.REQUESTED,
    )
}
