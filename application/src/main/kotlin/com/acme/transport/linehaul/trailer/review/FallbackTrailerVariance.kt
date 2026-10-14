package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback trailer variance is in its life cycle. */
enum class FallbackTrailerVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback trailer variance. Its code is unique within a tenant. */
data class FallbackTrailerVariance(
    var code: String,
    var name: String,
    var maximum: Double,
    var ceiling: Double,
    var loadKg: Double,
    var category: String,
    var routeCode: String,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var status: FallbackTrailerVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTrailerVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTrailerVarianceStatus): Boolean {
        when (status) {
            FallbackTrailerVarianceStatus.DRAFT -> return next == FallbackTrailerVarianceStatus.PENDING
            FallbackTrailerVarianceStatus.PENDING -> {
                when (next) {
                    FallbackTrailerVarianceStatus.SUSPENDED, FallbackTrailerVarianceStatus.CLOSED -> return true
                }
                return false
            }
            FallbackTrailerVarianceStatus.ACTIVE -> return next == FallbackTrailerVarianceStatus.SUSPENDED || next == FallbackTrailerVarianceStatus.CLOSED
            FallbackTrailerVarianceStatus.SUSPENDED -> return next == FallbackTrailerVarianceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackTrailerVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback trailer variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round(loadKg * maximum * 100) / 100

    fun snapshot(): FallbackTrailerVariance {
        return FallbackTrailerVariance(
            code = code,
            name = name,
            maximum = maximum,
            ceiling = ceiling,
            loadKg = loadKg,
            category = category,
            routeCode = routeCode,
            locale = locale,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback trailer variance in status draft. */
fun newFallbackTrailerVariance(code: String, name: String): FallbackTrailerVariance {
    return FallbackTrailerVariance(
        code = code,
        name = name,
        maximum = 0.0,
        ceiling = 0.0,
        loadKg = 0.0,
        category = "",
        routeCode = "",
        locale = "",
        attributes = mutableMapOf<String, String>(),
        status = FallbackTrailerVarianceStatus.DRAFT,
    )
}
