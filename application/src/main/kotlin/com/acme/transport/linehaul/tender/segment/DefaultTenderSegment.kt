package com.acme.transport.linehaul.tender.segment

import com.acme.transport.kit.text.normalize

// The default tender segment records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a default tender segment is in its life cycle. */
enum class DefaultTenderSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default tender segment. Its code is unique within a tenant. */
data class DefaultTenderSegment(
    var code: String,
    var label: String,
    var endsAt: Int,
    var region: String,
    var attributes: MutableMap<String, String>,
    var status: DefaultTenderSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTenderSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTenderSegmentStatus): Boolean {
        when (status) {
            DefaultTenderSegmentStatus.CREATED -> return next == DefaultTenderSegmentStatus.RETIRED
            DefaultTenderSegmentStatus.VALIDATED -> {
                when (next) {
                    DefaultTenderSegmentStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultTenderSegmentStatus.PUBLISHED -> return next == DefaultTenderSegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultTenderSegmentStatus) {
        if (!canMoveTo(next)) {
            error("default tender segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default tender segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): DefaultTenderSegment {
        return DefaultTenderSegment(
            code = code,
            label = label,
            endsAt = endsAt,
            region = region,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default tender segment in status created. */
fun newDefaultTenderSegment(code: String, label: String): DefaultTenderSegment {
    return DefaultTenderSegment(
        code = code,
        label = label,
        endsAt = 0,
        region = "",
        attributes = mutableMapOf<String, String>(),
        status = DefaultTenderSegmentStatus.CREATED,
    )
}
