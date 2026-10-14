package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock quota is in its life cycle. */
enum class CrossDockQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A cross dock quota as the cross dock team keeps it. */
data class CrossDockQuota(
    var code: String,
    var name: String,
    var active: Boolean,
    var amount: Double,
    var version: Int,
    var minimum: Double,
    var segment: String,
    var weight: Double,
    var status: CrossDockQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockQuotaStatus): Boolean {
        when (status) {
            CrossDockQuotaStatus.CREATED -> {
                when (next) {
                    CrossDockQuotaStatus.PUBLISHED -> return true
                }
                return false
            }
            CrossDockQuotaStatus.VALIDATED -> {
                when (next) {
                    CrossDockQuotaStatus.PUBLISHED, CrossDockQuotaStatus.RETIRED -> return true
                }
                return false
            }
            CrossDockQuotaStatus.PUBLISHED -> {
                when (next) {
                    CrossDockQuotaStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockQuotaStatus) {
        if (!canMoveTo(next)) {
            error("cross dock quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((weight - minimum) * 100) / 100

    fun snapshot(): CrossDockQuota {
        return CrossDockQuota(
            code = code,
            name = name,
            active = active,
            amount = amount,
            version = version,
            minimum = minimum,
            segment = segment,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a cross dock quota in status created. */
fun newCrossDockQuota(code: String, name: String): CrossDockQuota {
    return CrossDockQuota(
        code = code,
        name = name,
        active = false,
        amount = 0.0,
        version = 0,
        minimum = 0.0,
        segment = "",
        weight = 0.0,
        status = CrossDockQuotaStatus.CREATED,
    )
}
