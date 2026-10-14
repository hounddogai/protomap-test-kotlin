package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.text.normalize

/** Where a tiered redelivery override is in its life cycle. */
enum class TieredRedeliveryOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered redelivery override as the redelivery team keeps it. */
data class TieredRedeliveryOverride(
    var code: String,
    var name: String,
    var ownerId: String,
    var approved: Boolean,
    var label: String,
    var source: String,
    var shiftCode: String,
    var timezone: String,
    var weight: Double,
    var dock: String,
    var status: TieredRedeliveryOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRedeliveryOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRedeliveryOverrideStatus): Boolean {
        when (status) {
            TieredRedeliveryOverrideStatus.REQUESTED -> {
                when (next) {
                    TieredRedeliveryOverrideStatus.GRANTED, TieredRedeliveryOverrideStatus.REVOKED -> return true
                }
                return false
            }
            TieredRedeliveryOverrideStatus.GRANTED -> return next == TieredRedeliveryOverrideStatus.REVOKED || next == TieredRedeliveryOverrideStatus.EXPIRED
            TieredRedeliveryOverrideStatus.REVOKED -> {
                when (next) {
                    TieredRedeliveryOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRedeliveryOverrideStatus) {
        if (!canMoveTo(next)) {
            error("tiered redelivery override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun measuredValue(): Double = weight

    fun snapshot(): TieredRedeliveryOverride {
        return TieredRedeliveryOverride(
            code = code,
            name = name,
            ownerId = ownerId,
            approved = approved,
            label = label,
            source = source,
            shiftCode = shiftCode,
            timezone = timezone,
            weight = weight,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered redelivery override in status requested. */
fun newTieredRedeliveryOverride(code: String, name: String): TieredRedeliveryOverride {
    return TieredRedeliveryOverride(
        code = code,
        name = name,
        ownerId = "",
        approved = false,
        label = "",
        source = "",
        shiftCode = "",
        timezone = "",
        weight = 0.0,
        dock = "",
        status = TieredRedeliveryOverrideStatus.REQUESTED,
    )
}
