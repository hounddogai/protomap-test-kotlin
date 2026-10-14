package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched trailer yard budget records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a batched trailer yard budget is in its life cycle. */
enum class BatchedTrailerYardBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched trailer yard budget as the trailer yard team keeps it. */
data class BatchedTrailerYardBudget(
    var code: String,
    var name: String,
    var weight: Double,
    var phone: String,
    var tags: MutableList<String>,
    var sequence: Int,
    var floorValue: Double,
    var dock: String,
    var visible: Boolean,
    var status: BatchedTrailerYardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerYardBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerYardBudgetStatus): Boolean {
        when (status) {
            BatchedTrailerYardBudgetStatus.PROPOSED -> return next == BatchedTrailerYardBudgetStatus.APPROVED || next == BatchedTrailerYardBudgetStatus.WITHDRAWN
            BatchedTrailerYardBudgetStatus.APPROVED -> {
                when (next) {
                    BatchedTrailerYardBudgetStatus.REJECTED -> return true
                }
                return false
            }
            BatchedTrailerYardBudgetStatus.REJECTED -> return next == BatchedTrailerYardBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedTrailerYardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer yard budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched trailer yard budget"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((weight + sequence.toDouble()) * 100) / 100

    fun snapshot(): BatchedTrailerYardBudget {
        return BatchedTrailerYardBudget(
            code = code,
            name = name,
            weight = weight,
            phone = phone,
            tags = tags,
            sequence = sequence,
            floorValue = floorValue,
            dock = dock,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched trailer yard budget in status proposed. */
fun newBatchedTrailerYardBudget(code: String, name: String): BatchedTrailerYardBudget {
    return BatchedTrailerYardBudget(
        code = code,
        name = name,
        weight = 0.0,
        phone = "",
        tags = mutableListOf<String>(),
        sequence = 0,
        floorValue = 0.0,
        dock = "",
        visible = false,
        status = BatchedTrailerYardBudgetStatus.PROPOSED,
    )
}
