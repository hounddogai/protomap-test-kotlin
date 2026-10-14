package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity preference is in its life cycle. */
enum class CapacityPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capacity preference, with what the planners need to act on it. */
data class CapacityPreference(
    var code: String,
    var name: String,
    var rate: Double,
    var expiresAt: Int,
    var tags: MutableList<String>,
    var reviewer: String,
    var attempts: Int,
    var category: String,
    var status: CapacityPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityPreferenceStatus): Boolean {
        when (status) {
            CapacityPreferenceStatus.CREATED -> return next == CapacityPreferenceStatus.PUBLISHED || next == CapacityPreferenceStatus.RETIRED
            CapacityPreferenceStatus.VALIDATED -> return next == CapacityPreferenceStatus.PUBLISHED || next == CapacityPreferenceStatus.RETIRED
            CapacityPreferenceStatus.PUBLISHED -> return next == CapacityPreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CapacityPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("capacity preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round(attempts.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): CapacityPreference {
        return CapacityPreference(
            code = code,
            name = name,
            rate = rate,
            expiresAt = expiresAt,
            tags = tags,
            reviewer = reviewer,
            attempts = attempts,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capacity preference in status created. */
fun newCapacityPreference(code: String, name: String): CapacityPreference {
    return CapacityPreference(
        code = code,
        name = name,
        rate = 0.0,
        expiresAt = 0,
        tags = mutableListOf<String>(),
        reviewer = "",
        attempts = 0,
        category = "",
        status = CapacityPreferenceStatus.CREATED,
    )
}
