package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub bundle is in its life cycle. */
enum class HubBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hub bundle. Its code is unique within a tenant. */
data class HubBundle(
    var code: String,
    var label: String,
    var depot: String,
    var channel: String,
    var description: String,
    var timezone: String,
    var amount: Double,
    var stopCount: Int,
    var phone: String,
    var country: String,
    var status: HubBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubBundleStatus): Boolean {
        when (status) {
            HubBundleStatus.CREATED -> {
                when (next) {
                    HubBundleStatus.VALIDATED -> return true
                }
                return false
            }
            HubBundleStatus.VALIDATED -> {
                when (next) {
                    HubBundleStatus.PUBLISHED -> return true
                }
                return false
            }
            HubBundleStatus.PUBLISHED -> {
                when (next) {
                    HubBundleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubBundleStatus) {
        if (!canMoveTo(next)) {
            error("hub bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round(amount * stopCount.toDouble() * 100) / 100

    fun snapshot(): HubBundle {
        return HubBundle(
            code = code,
            label = label,
            depot = depot,
            channel = channel,
            description = description,
            timezone = timezone,
            amount = amount,
            stopCount = stopCount,
            phone = phone,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a hub bundle in status created. */
fun newHubBundle(code: String, label: String): HubBundle {
    return HubBundle(
        code = code,
        label = label,
        depot = "",
        channel = "",
        description = "",
        timezone = "",
        amount = 0.0,
        stopCount = 0,
        phone = "",
        country = "",
        status = HubBundleStatus.CREATED,
    )
}
