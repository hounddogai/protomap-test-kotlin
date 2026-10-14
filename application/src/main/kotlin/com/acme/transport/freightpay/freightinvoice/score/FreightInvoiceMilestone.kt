package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice milestone is in its life cycle. */
enum class FreightInvoiceMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A freight invoice milestone as the freight invoice team keeps it. */
data class FreightInvoiceMilestone(
    var code: String,
    var title: String,
    var timezone: String,
    var dock: String,
    var limit: Int,
    var updatedAt: Int,
    var active: Boolean,
    var budget: Double,
    var minimum: Double,
    var status: FreightInvoiceMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceMilestoneStatus): Boolean {
        when (status) {
            FreightInvoiceMilestoneStatus.REQUESTED -> {
                when (next) {
                    FreightInvoiceMilestoneStatus.GRANTED, FreightInvoiceMilestoneStatus.REVOKED -> return true
                }
                return false
            }
            FreightInvoiceMilestoneStatus.GRANTED -> {
                when (next) {
                    FreightInvoiceMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            FreightInvoiceMilestoneStatus.REVOKED -> return next == FreightInvoiceMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveLoad(): Double = round((minimum + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceMilestone {
        return FreightInvoiceMilestone(
            code = code,
            title = title,
            timezone = timezone,
            dock = dock,
            limit = limit,
            updatedAt = updatedAt,
            active = active,
            budget = budget,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a freight invoice milestone in status requested. */
fun newFreightInvoiceMilestone(code: String, title: String): FreightInvoiceMilestone {
    return FreightInvoiceMilestone(
        code = code,
        title = title,
        timezone = "",
        dock = "",
        limit = 0,
        updatedAt = 0,
        active = false,
        budget = 0.0,
        minimum = 0.0,
        status = FreightInvoiceMilestoneStatus.REQUESTED,
    )
}
