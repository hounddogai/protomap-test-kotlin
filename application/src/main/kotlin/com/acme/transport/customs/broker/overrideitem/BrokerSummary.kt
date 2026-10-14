package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker summary is in its life cycle. */
enum class BrokerSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A broker summary as the broker team keeps it. */
data class BrokerSummary(
    var code: String,
    var name: String,
    var budget: Double,
    var weight: Double,
    var phone: String,
    var quantity: Int,
    var country: String,
    var ownerId: String,
    var sequence: Int,
    var vehicleId: String,
    var capacity: Int,
    var status: BrokerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerSummaryStatus): Boolean {
        when (status) {
            BrokerSummaryStatus.CREATED -> return next == BrokerSummaryStatus.RETIRED
            BrokerSummaryStatus.VALIDATED -> return next == BrokerSummaryStatus.PUBLISHED || next == BrokerSummaryStatus.RETIRED
            BrokerSummaryStatus.PUBLISHED -> return next == BrokerSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BrokerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("broker summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((budget + weight) * 100) / 100

    fun snapshot(): BrokerSummary {
        return BrokerSummary(
            code = code,
            name = name,
            budget = budget,
            weight = weight,
            phone = phone,
            quantity = quantity,
            country = country,
            ownerId = ownerId,
            sequence = sequence,
            vehicleId = vehicleId,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a broker summary in status created. */
fun newBrokerSummary(code: String, name: String): BrokerSummary {
    return BrokerSummary(
        code = code,
        name = name,
        budget = 0.0,
        weight = 0.0,
        phone = "",
        quantity = 0,
        country = "",
        ownerId = "",
        sequence = 0,
        vehicleId = "",
        capacity = 0,
        status = BrokerSummaryStatus.CREATED,
    )
}
