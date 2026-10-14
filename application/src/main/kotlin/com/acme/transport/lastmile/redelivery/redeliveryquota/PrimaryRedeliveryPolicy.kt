package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary redelivery policy is in its life cycle. */
enum class PrimaryRedeliveryPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary redelivery policy as the redelivery team keeps it. */
data class PrimaryRedeliveryPolicy(
    var code: String,
    var name: String,
    var archived: Boolean,
    var carrier: String,
    var amount: Double,
    var ceiling: Double,
    var label: String,
    var rate: Double,
    var title: String,
    var floorValue: Double,
    var country: String,
    var status: PrimaryRedeliveryPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRedeliveryPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRedeliveryPolicyStatus): Boolean {
        when (status) {
            PrimaryRedeliveryPolicyStatus.CREATED -> {
                when (next) {
                    PrimaryRedeliveryPolicyStatus.VALIDATED, PrimaryRedeliveryPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            PrimaryRedeliveryPolicyStatus.VALIDATED -> {
                when (next) {
                    PrimaryRedeliveryPolicyStatus.PUBLISHED, PrimaryRedeliveryPolicyStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryRedeliveryPolicyStatus.PUBLISHED -> {
                when (next) {
                    PrimaryRedeliveryPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRedeliveryPolicyStatus) {
        if (!canMoveTo(next)) {
            error("primary redelivery policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary redelivery policy"} $code"

    fun weightedExposure(): Double = round((rate - amount) * 100) / 100

    fun snapshot(): PrimaryRedeliveryPolicy {
        return PrimaryRedeliveryPolicy(
            code = code,
            name = name,
            archived = archived,
            carrier = carrier,
            amount = amount,
            ceiling = ceiling,
            label = label,
            rate = rate,
            title = title,
            floorValue = floorValue,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary redelivery policy in status created. */
fun newPrimaryRedeliveryPolicy(code: String, name: String): PrimaryRedeliveryPolicy {
    return PrimaryRedeliveryPolicy(
        code = code,
        name = name,
        archived = false,
        carrier = "",
        amount = 0.0,
        ceiling = 0.0,
        label = "",
        rate = 0.0,
        title = "",
        floorValue = 0.0,
        country = "",
        status = PrimaryRedeliveryPolicyStatus.CREATED,
    )
}
