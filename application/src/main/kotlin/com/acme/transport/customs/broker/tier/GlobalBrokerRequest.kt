package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global broker request is in its life cycle. */
enum class GlobalBrokerRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global broker request as the broker team keeps it. */
data class GlobalBrokerRequest(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var cost: Double,
    var contactEmail: String,
    var loadKg: Double,
    var timezone: String,
    var status: GlobalBrokerRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBrokerRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBrokerRequestStatus): Boolean {
        when (status) {
            GlobalBrokerRequestStatus.NEW -> {
                when (next) {
                    GlobalBrokerRequestStatus.RUNNING, GlobalBrokerRequestStatus.DONE -> return true
                }
                return false
            }
            GlobalBrokerRequestStatus.QUEUED -> {
                when (next) {
                    GlobalBrokerRequestStatus.FAILED -> return true
                }
                return false
            }
            GlobalBrokerRequestStatus.RUNNING -> return next == GlobalBrokerRequestStatus.DONE || next == GlobalBrokerRequestStatus.FAILED
            GlobalBrokerRequestStatus.DONE -> return next == GlobalBrokerRequestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalBrokerRequestStatus) {
        if (!canMoveTo(next)) {
            error("global broker request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round(cost * fuelLiters * 100) / 100

    fun snapshot(): GlobalBrokerRequest {
        return GlobalBrokerRequest(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            cost = cost,
            contactEmail = contactEmail,
            loadKg = loadKg,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global broker request in status new. */
fun newGlobalBrokerRequest(code: String, label: String): GlobalBrokerRequest {
    return GlobalBrokerRequest(
        code = code,
        label = label,
        fuelLiters = 0.0,
        cost = 0.0,
        contactEmail = "",
        loadKg = 0.0,
        timezone = "",
        status = GlobalBrokerRequestStatus.NEW,
    )
}
