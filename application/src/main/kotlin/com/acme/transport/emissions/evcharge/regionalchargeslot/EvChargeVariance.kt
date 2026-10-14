package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.text.normalize

/** Where a ev charge variance is in its life cycle. */
enum class EvChargeVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One ev charge variance, with what the planners need to act on it. */
data class EvChargeVariance(
    var code: String,
    var name: String,
    var channel: String,
    var trailerId: String,
    var contactEmail: String,
    var segment: String,
    var region: String,
    var quantity: Int,
    var status: EvChargeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeVarianceStatus): Boolean {
        when (status) {
            EvChargeVarianceStatus.CREATED -> {
                when (next) {
                    EvChargeVarianceStatus.VALIDATED, EvChargeVarianceStatus.PUBLISHED -> return true
                }
                return false
            }
            EvChargeVarianceStatus.VALIDATED -> {
                when (next) {
                    EvChargeVarianceStatus.PUBLISHED, EvChargeVarianceStatus.RETIRED -> return true
                }
                return false
            }
            EvChargeVarianceStatus.PUBLISHED -> return next == EvChargeVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: EvChargeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("ev charge variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): EvChargeVariance {
        return EvChargeVariance(
            code = code,
            name = name,
            channel = channel,
            trailerId = trailerId,
            contactEmail = contactEmail,
            segment = segment,
            region = region,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a ev charge variance in status created. */
fun newEvChargeVariance(code: String, name: String): EvChargeVariance {
    return EvChargeVariance(
        code = code,
        name = name,
        channel = "",
        trailerId = "",
        contactEmail = "",
        segment = "",
        region = "",
        quantity = 0,
        status = EvChargeVarianceStatus.CREATED,
    )
}
