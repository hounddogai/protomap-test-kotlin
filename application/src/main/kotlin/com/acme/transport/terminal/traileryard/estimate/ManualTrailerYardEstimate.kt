package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual trailer yard estimate records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a manual trailer yard estimate is in its life cycle. */
enum class ManualTrailerYardEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual trailer yard estimate. Its code is unique within a tenant. */
data class ManualTrailerYardEstimate(
    var code: String,
    var name: String,
    var locale: String,
    var version: Int,
    var score: Double,
    var margin: Double,
    var title: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var tags: MutableList<String>,
    var status: ManualTrailerYardEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerYardEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerYardEstimateStatus): Boolean {
        when (status) {
            ManualTrailerYardEstimateStatus.PROPOSED -> return next == ManualTrailerYardEstimateStatus.APPROVED
            ManualTrailerYardEstimateStatus.APPROVED -> return next == ManualTrailerYardEstimateStatus.WITHDRAWN
            ManualTrailerYardEstimateStatus.REJECTED -> return next == ManualTrailerYardEstimateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualTrailerYardEstimateStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer yard estimate $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($name)"

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

    fun projectedTotal(): Double = round(version.toDouble() * score * 100) / 100

    fun snapshot(): ManualTrailerYardEstimate {
        return ManualTrailerYardEstimate(
            code = code,
            name = name,
            locale = locale,
            version = version,
            score = score,
            margin = margin,
            title = title,
            label = label,
            attributes = attributes,
            capacity = capacity,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual trailer yard estimate in status proposed. */
fun newManualTrailerYardEstimate(code: String, name: String): ManualTrailerYardEstimate {
    return ManualTrailerYardEstimate(
        code = code,
        name = name,
        locale = "",
        version = 0,
        score = 0.0,
        margin = 0.0,
        title = "",
        label = "",
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        tags = mutableListOf<String>(),
        status = ManualTrailerYardEstimateStatus.PROPOSED,
    )
}
