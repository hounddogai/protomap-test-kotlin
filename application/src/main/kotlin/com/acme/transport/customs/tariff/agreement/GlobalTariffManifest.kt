package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tariff manifest is in its life cycle. */
enum class GlobalTariffManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global tariff manifest as the tariff team keeps it. */
data class GlobalTariffManifest(
    var code: String,
    var name: String,
    var maximum: Double,
    var priority: Int,
    var threshold: Double,
    var dock: String,
    var limit: Int,
    var graceDays: Int,
    var driverId: String,
    var weight: Double,
    var status: GlobalTariffManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTariffManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTariffManifestStatus): Boolean {
        when (status) {
            GlobalTariffManifestStatus.NEW -> {
                when (next) {
                    GlobalTariffManifestStatus.QUEUED -> return true
                }
                return false
            }
            GlobalTariffManifestStatus.QUEUED -> return next == GlobalTariffManifestStatus.RUNNING || next == GlobalTariffManifestStatus.DONE
            GlobalTariffManifestStatus.RUNNING -> {
                when (next) {
                    GlobalTariffManifestStatus.DONE, GlobalTariffManifestStatus.FAILED -> return true
                }
                return false
            }
            GlobalTariffManifestStatus.DONE -> return next == GlobalTariffManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalTariffManifestStatus) {
        if (!canMoveTo(next)) {
            error("global tariff manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((threshold - weight) * 100) / 100

    fun snapshot(): GlobalTariffManifest {
        return GlobalTariffManifest(
            code = code,
            name = name,
            maximum = maximum,
            priority = priority,
            threshold = threshold,
            dock = dock,
            limit = limit,
            graceDays = graceDays,
            driverId = driverId,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global tariff manifest in status new. */
fun newGlobalTariffManifest(code: String, name: String): GlobalTariffManifest {
    return GlobalTariffManifest(
        code = code,
        name = name,
        maximum = 0.0,
        priority = 0,
        threshold = 0.0,
        dock = "",
        limit = 0,
        graceDays = 0,
        driverId = "",
        weight = 0.0,
        status = GlobalTariffManifestStatus.NEW,
    )
}
