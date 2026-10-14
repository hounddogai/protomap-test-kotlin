package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional broker summary is in its life cycle. */
enum class RegionalBrokerSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional broker summary. Its code is unique within a tenant. */
data class RegionalBrokerSummary(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var unit: String,
    var floorValue: Double,
    var ownerId: String,
    var discount: Double,
    var status: RegionalBrokerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBrokerSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBrokerSummaryStatus): Boolean {
        when (status) {
            RegionalBrokerSummaryStatus.DRAFT -> return next == RegionalBrokerSummaryStatus.CLOSED
            RegionalBrokerSummaryStatus.PENDING -> return next == RegionalBrokerSummaryStatus.ACTIVE
            RegionalBrokerSummaryStatus.ACTIVE -> return next == RegionalBrokerSummaryStatus.SUSPENDED || next == RegionalBrokerSummaryStatus.CLOSED
            RegionalBrokerSummaryStatus.SUSPENDED -> {
                when (next) {
                    RegionalBrokerSummaryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalBrokerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("regional broker summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((durationMinutes.toDouble() + floorValue) * 100) / 100

    fun snapshot(): RegionalBrokerSummary {
        return RegionalBrokerSummary(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            unit = unit,
            floorValue = floorValue,
            ownerId = ownerId,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional broker summary in status draft. */
fun newRegionalBrokerSummary(code: String, name: String): RegionalBrokerSummary {
    return RegionalBrokerSummary(
        code = code,
        name = name,
        durationMinutes = 0,
        unit = "",
        floorValue = 0.0,
        ownerId = "",
        discount = 0.0,
        status = RegionalBrokerSummaryStatus.DRAFT,
    )
}
