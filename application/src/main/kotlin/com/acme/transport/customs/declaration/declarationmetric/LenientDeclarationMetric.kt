package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient declaration metric records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a lenient declaration metric is in its life cycle. */
enum class LenientDeclarationMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient declaration metric. Its code is unique within a tenant. */
data class LenientDeclarationMetric(
    var code: String,
    var title: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var checksum: String,
    var country: String,
    var shiftCode: String,
    var createdAt: Int,
    var timezone: String,
    var version: Int,
    var status: LenientDeclarationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDeclarationMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDeclarationMetricStatus): Boolean {
        when (status) {
            LenientDeclarationMetricStatus.OPEN -> return next == LenientDeclarationMetricStatus.ARCHIVED
            LenientDeclarationMetricStatus.IN_REVIEW -> {
                when (next) {
                    LenientDeclarationMetricStatus.RESOLVED, LenientDeclarationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientDeclarationMetricStatus.RESOLVED -> {
                when (next) {
                    LenientDeclarationMetricStatus.REOPENED -> return true
                }
                return false
            }
            LenientDeclarationMetricStatus.REOPENED -> {
                when (next) {
                    LenientDeclarationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDeclarationMetricStatus) {
        if (!canMoveTo(next)) {
            error("lenient declaration metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"lenient declaration metric"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round(version.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): LenientDeclarationMetric {
        return LenientDeclarationMetric(
            code = code,
            title = title,
            vehicleId = vehicleId,
            attributes = attributes,
            checksum = checksum,
            country = country,
            shiftCode = shiftCode,
            createdAt = createdAt,
            timezone = timezone,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient declaration metric in status open. */
fun newLenientDeclarationMetric(code: String, title: String): LenientDeclarationMetric {
    return LenientDeclarationMetric(
        code = code,
        title = title,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        checksum = "",
        country = "",
        shiftCode = "",
        createdAt = 0,
        timezone = "",
        version = 0,
        status = LenientDeclarationMetricStatus.OPEN,
    )
}
