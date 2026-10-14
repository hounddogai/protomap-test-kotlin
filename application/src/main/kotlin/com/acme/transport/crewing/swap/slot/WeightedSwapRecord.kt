package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.text.normalize

/** Where a weighted swap record is in its life cycle. */
enum class WeightedSwapRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted swap record. Its code is unique within a tenant. */
data class WeightedSwapRecord(
    var code: String,
    var label: String,
    var reviewer: String,
    var category: String,
    var archived: Boolean,
    var externalRef: String,
    var attributes: MutableMap<String, String>,
    var dock: String,
    var amount: Double,
    var status: WeightedSwapRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSwapRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSwapRecordStatus): Boolean {
        when (status) {
            WeightedSwapRecordStatus.OPEN -> {
                when (next) {
                    WeightedSwapRecordStatus.RESOLVED, WeightedSwapRecordStatus.REOPENED -> return true
                }
                return false
            }
            WeightedSwapRecordStatus.IN_REVIEW -> return next == WeightedSwapRecordStatus.RESOLVED || next == WeightedSwapRecordStatus.REOPENED
            WeightedSwapRecordStatus.RESOLVED -> {
                when (next) {
                    WeightedSwapRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedSwapRecordStatus.REOPENED -> return next == WeightedSwapRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedSwapRecordStatus) {
        if (!canMoveTo(next)) {
            error("weighted swap record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = amount

    fun snapshot(): WeightedSwapRecord {
        return WeightedSwapRecord(
            code = code,
            label = label,
            reviewer = reviewer,
            category = category,
            archived = archived,
            externalRef = externalRef,
            attributes = attributes,
            dock = dock,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted swap record in status open. */
fun newWeightedSwapRecord(code: String, label: String): WeightedSwapRecord {
    return WeightedSwapRecord(
        code = code,
        label = label,
        reviewer = "",
        category = "",
        archived = false,
        externalRef = "",
        attributes = mutableMapOf<String, String>(),
        dock = "",
        amount = 0.0,
        status = WeightedSwapRecordStatus.OPEN,
    )
}
