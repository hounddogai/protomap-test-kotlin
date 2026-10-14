package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled backhaul digest records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a scheduled backhaul digest is in its life cycle. */
enum class ScheduledBackhaulDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled backhaul digest. Its code is unique within a tenant. */
data class ScheduledBackhaulDigest(
    var code: String,
    var title: String,
    var locale: String,
    var cost: Double,
    var vehicleId: String,
    var externalRef: String,
    var category: String,
    var pallets: Int,
    var priority: Int,
    var status: ScheduledBackhaulDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBackhaulDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBackhaulDigestStatus): Boolean {
        when (status) {
            ScheduledBackhaulDigestStatus.NEW -> return next == ScheduledBackhaulDigestStatus.QUEUED
            ScheduledBackhaulDigestStatus.QUEUED -> {
                when (next) {
                    ScheduledBackhaulDigestStatus.DONE, ScheduledBackhaulDigestStatus.FAILED -> return true
                }
                return false
            }
            ScheduledBackhaulDigestStatus.RUNNING -> return next == ScheduledBackhaulDigestStatus.FAILED
            ScheduledBackhaulDigestStatus.DONE -> return next == ScheduledBackhaulDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledBackhaulDigestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled backhaul digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round(pallets.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): ScheduledBackhaulDigest {
        return ScheduledBackhaulDigest(
            code = code,
            title = title,
            locale = locale,
            cost = cost,
            vehicleId = vehicleId,
            externalRef = externalRef,
            category = category,
            pallets = pallets,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled backhaul digest in status new. */
fun newScheduledBackhaulDigest(code: String, title: String): ScheduledBackhaulDigest {
    return ScheduledBackhaulDigest(
        code = code,
        title = title,
        locale = "",
        cost = 0.0,
        vehicleId = "",
        externalRef = "",
        category = "",
        pallets = 0,
        priority = 0,
        status = ScheduledBackhaulDigestStatus.NEW,
    )
}
