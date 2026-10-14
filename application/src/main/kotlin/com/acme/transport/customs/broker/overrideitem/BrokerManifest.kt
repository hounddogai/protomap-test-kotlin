package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker manifest is in its life cycle. */
enum class BrokerManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A broker manifest. Its code is unique within a tenant. */
data class BrokerManifest(
    var code: String,
    var name: String,
    var graceDays: Int,
    var budget: Double,
    var dock: String,
    var label: String,
    var driverId: String,
    var phone: String,
    var status: BrokerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerManifestStatus): Boolean {
        when (status) {
            BrokerManifestStatus.NEW -> return next == BrokerManifestStatus.QUEUED
            BrokerManifestStatus.QUEUED -> return next == BrokerManifestStatus.RUNNING || next == BrokerManifestStatus.FAILED
            BrokerManifestStatus.RUNNING -> return next == BrokerManifestStatus.DONE || next == BrokerManifestStatus.FAILED
            BrokerManifestStatus.DONE -> return next == BrokerManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BrokerManifestStatus) {
        if (!canMoveTo(next)) {
            error("broker manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun weightedTotal(): Double = round((graceDays.toDouble() + budget) * 100) / 100

    fun snapshot(): BrokerManifest {
        return BrokerManifest(
            code = code,
            name = name,
            graceDays = graceDays,
            budget = budget,
            dock = dock,
            label = label,
            driverId = driverId,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a broker manifest in status new. */
fun newBrokerManifest(code: String, name: String): BrokerManifest {
    return BrokerManifest(
        code = code,
        name = name,
        graceDays = 0,
        budget = 0.0,
        dock = "",
        label = "",
        driverId = "",
        phone = "",
        status = BrokerManifestStatus.NEW,
    )
}
