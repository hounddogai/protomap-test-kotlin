package com.acme.transport.network.servicearea.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service area summary records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area summary is in its life cycle. */
enum class ServiceAreaSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A service area summary. Its code is unique within a tenant. */
data class ServiceAreaSummary(
    var code: String,
    var name: String,
    var endsAt: Int,
    var attributes: MutableMap<String, String>,
    var stopCount: Int,
    var archived: Boolean,
    var routeCode: String,
    var status: ServiceAreaSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaSummaryStatus): Boolean {
        when (status) {
            ServiceAreaSummaryStatus.CREATED -> {
                when (next) {
                    ServiceAreaSummaryStatus.VALIDATED, ServiceAreaSummaryStatus.RETIRED -> return true
                }
                return false
            }
            ServiceAreaSummaryStatus.VALIDATED -> {
                when (next) {
                    ServiceAreaSummaryStatus.PUBLISHED -> return true
                }
                return false
            }
            ServiceAreaSummaryStatus.PUBLISHED -> {
                when (next) {
                    ServiceAreaSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaSummaryStatus) {
        if (!canMoveTo(next)) {
            error("service area summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"service area summary"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((stopCount.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaSummary {
        return ServiceAreaSummary(
            code = code,
            name = name,
            endsAt = endsAt,
            attributes = attributes,
            stopCount = stopCount,
            archived = archived,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service area summary in status created. */
fun newServiceAreaSummary(code: String, name: String): ServiceAreaSummary {
    return ServiceAreaSummary(
        code = code,
        name = name,
        endsAt = 0,
        attributes = mutableMapOf<String, String>(),
        stopCount = 0,
        archived = false,
        routeCode = "",
        status = ServiceAreaSummaryStatus.CREATED,
    )
}
