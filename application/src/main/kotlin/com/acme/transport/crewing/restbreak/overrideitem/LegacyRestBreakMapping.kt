package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy rest break mapping is in its life cycle. */
enum class LegacyRestBreakMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One legacy rest break mapping, with what the planners need to act on it. */
data class LegacyRestBreakMapping(
    var code: String,
    var title: String,
    var ratio: Double,
    var attributes: MutableMap<String, String>,
    var loadKg: Double,
    var revision: Int,
    var name: String,
    var pinned: Boolean,
    var batchSize: Int,
    var status: LegacyRestBreakMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRestBreakMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRestBreakMappingStatus): Boolean {
        when (status) {
            LegacyRestBreakMappingStatus.PROPOSED -> {
                when (next) {
                    LegacyRestBreakMappingStatus.REJECTED, LegacyRestBreakMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyRestBreakMappingStatus.APPROVED -> return next == LegacyRestBreakMappingStatus.REJECTED || next == LegacyRestBreakMappingStatus.WITHDRAWN
            LegacyRestBreakMappingStatus.REJECTED -> return next == LegacyRestBreakMappingStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyRestBreakMappingStatus) {
        if (!canMoveTo(next)) {
            error("legacy rest break mapping $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round(ratio * loadKg * 100) / 100

    fun snapshot(): LegacyRestBreakMapping {
        return LegacyRestBreakMapping(
            code = code,
            title = title,
            ratio = ratio,
            attributes = attributes,
            loadKg = loadKg,
            revision = revision,
            name = name,
            pinned = pinned,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy rest break mapping in status proposed. */
fun newLegacyRestBreakMapping(code: String, title: String): LegacyRestBreakMapping {
    return LegacyRestBreakMapping(
        code = code,
        title = title,
        ratio = 0.0,
        attributes = mutableMapOf<String, String>(),
        loadKg = 0.0,
        revision = 0,
        name = "",
        pinned = false,
        batchSize = 0,
        status = LegacyRestBreakMappingStatus.PROPOSED,
    )
}
