package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient near miss batch is in its life cycle. */
enum class LenientNearMissBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lenient near miss batch and its current status. */
data class LenientNearMissBatch(
    var code: String,
    var title: String,
    var retries: Int,
    var tags: MutableList<String>,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var minimum: Double,
    var reviewer: String,
    var fuelLiters: Double,
    var status: LenientNearMissBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNearMissBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNearMissBatchStatus): Boolean {
        when (status) {
            LenientNearMissBatchStatus.PROPOSED -> {
                when (next) {
                    LenientNearMissBatchStatus.APPROVED, LenientNearMissBatchStatus.REJECTED -> return true
                }
                return false
            }
            LenientNearMissBatchStatus.APPROVED -> return next == LenientNearMissBatchStatus.REJECTED || next == LenientNearMissBatchStatus.WITHDRAWN
            LenientNearMissBatchStatus.REJECTED -> return next == LenientNearMissBatchStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientNearMissBatchStatus) {
        if (!canMoveTo(next)) {
            error("lenient near miss batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((fuelLiters - minimum) * 100) / 100

    fun snapshot(): LenientNearMissBatch {
        return LenientNearMissBatch(
            code = code,
            title = title,
            retries = retries,
            tags = tags,
            attributes = attributes,
            approved = approved,
            minimum = minimum,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient near miss batch in status proposed. */
fun newLenientNearMissBatch(code: String, title: String): LenientNearMissBatch {
    return LenientNearMissBatch(
        code = code,
        title = title,
        retries = 0,
        tags = mutableListOf<String>(),
        attributes = mutableMapOf<String, String>(),
        approved = false,
        minimum = 0.0,
        reviewer = "",
        fuelLiters = 0.0,
        status = LenientNearMissBatchStatus.PROPOSED,
    )
}
