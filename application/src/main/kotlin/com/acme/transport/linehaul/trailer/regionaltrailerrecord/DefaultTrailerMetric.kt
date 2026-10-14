package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.text.normalize

/** Where a default trailer metric is in its life cycle. */
enum class DefaultTrailerMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default trailer metric. Its code is unique within a tenant. */
data class DefaultTrailerMetric(
    var code: String,
    var name: String,
    var dock: String,
    var tags: MutableList<String>,
    var rate: Double,
    var description: String,
    var status: DefaultTrailerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTrailerMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTrailerMetricStatus): Boolean {
        when (status) {
            DefaultTrailerMetricStatus.REQUESTED -> return next == DefaultTrailerMetricStatus.REVOKED
            DefaultTrailerMetricStatus.GRANTED -> {
                when (next) {
                    DefaultTrailerMetricStatus.REVOKED, DefaultTrailerMetricStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultTrailerMetricStatus.REVOKED -> return next == DefaultTrailerMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultTrailerMetricStatus) {
        if (!canMoveTo(next)) {
            error("default trailer metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = rate

    fun snapshot(): DefaultTrailerMetric {
        return DefaultTrailerMetric(
            code = code,
            name = name,
            dock = dock,
            tags = tags,
            rate = rate,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default trailer metric in status requested. */
fun newDefaultTrailerMetric(code: String, name: String): DefaultTrailerMetric {
    return DefaultTrailerMetric(
        code = code,
        name = name,
        dock = "",
        tags = mutableListOf<String>(),
        rate = 0.0,
        description = "",
        status = DefaultTrailerMetricStatus.REQUESTED,
    )
}
