package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub variance is in its life cycle. */
enum class HubVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hub variance as the hub team keeps it. */
data class HubVariance(
    var code: String,
    var name: String,
    var priority: Int,
    var loadKg: Double,
    var dock: String,
    var createdAt: Int,
    var ratio: Double,
    var unit: String,
    var status: HubVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubVarianceStatus): Boolean {
        when (status) {
            HubVarianceStatus.CREATED -> return next == HubVarianceStatus.VALIDATED
            HubVarianceStatus.VALIDATED -> return next == HubVarianceStatus.PUBLISHED || next == HubVarianceStatus.RETIRED
            HubVarianceStatus.PUBLISHED -> return next == HubVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: HubVarianceStatus) {
        if (!canMoveTo(next)) {
            error("hub variance $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netTotal(): Double = round((priority.toDouble() - ratio) * 100) / 100

    fun snapshot(): HubVariance {
        return HubVariance(
            code = code,
            name = name,
            priority = priority,
            loadKg = loadKg,
            dock = dock,
            createdAt = createdAt,
            ratio = ratio,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a hub variance in status created. */
fun newHubVariance(code: String, name: String): HubVariance {
    return HubVariance(
        code = code,
        name = name,
        priority = 0,
        loadKg = 0.0,
        dock = "",
        createdAt = 0,
        ratio = 0.0,
        unit = "",
        status = HubVarianceStatus.CREATED,
    )
}
