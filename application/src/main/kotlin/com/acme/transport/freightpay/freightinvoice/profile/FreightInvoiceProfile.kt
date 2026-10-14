package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.text.normalize

// The freight invoice profile records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice profile is in its life cycle. */
enum class FreightInvoiceProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A freight invoice profile as the freight invoice team keeps it. */
data class FreightInvoiceProfile(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var ownerId: String,
    var tags: MutableList<String>,
    var depot: String,
    var trailerId: String,
    var status: FreightInvoiceProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceProfileStatus): Boolean {
        when (status) {
            FreightInvoiceProfileStatus.PLANNED -> return next == FreightInvoiceProfileStatus.IN_PROGRESS || next == FreightInvoiceProfileStatus.COMPLETED
            FreightInvoiceProfileStatus.SCHEDULED -> {
                when (next) {
                    FreightInvoiceProfileStatus.IN_PROGRESS -> return true
                }
                return false
            }
            FreightInvoiceProfileStatus.IN_PROGRESS -> {
                when (next) {
                    FreightInvoiceProfileStatus.COMPLETED, FreightInvoiceProfileStatus.CANCELLED -> return true
                }
                return false
            }
            FreightInvoiceProfileStatus.COMPLETED -> {
                when (next) {
                    FreightInvoiceProfileStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceProfileStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): FreightInvoiceProfile {
        return FreightInvoiceProfile(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            ownerId = ownerId,
            tags = tags,
            depot = depot,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a freight invoice profile in status planned. */
fun newFreightInvoiceProfile(code: String, title: String): FreightInvoiceProfile {
    return FreightInvoiceProfile(
        code = code,
        title = title,
        durationMinutes = 0,
        ownerId = "",
        tags = mutableListOf<String>(),
        depot = "",
        trailerId = "",
        status = FreightInvoiceProfileStatus.PLANNED,
    )
}
