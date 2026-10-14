package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break batch is in its life cycle. */
enum class RestBreakBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rest break batch as the rest break team keeps it. */
data class RestBreakBatch(
    var code: String,
    var label: String,
    var attempts: Int,
    var visible: Boolean,
    var externalRef: String,
    var quantity: Int,
    var category: String,
    var approved: Boolean,
    var status: RestBreakBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakBatchStatus): Boolean {
        when (status) {
            RestBreakBatchStatus.CREATED -> {
                when (next) {
                    RestBreakBatchStatus.PUBLISHED -> return true
                }
                return false
            }
            RestBreakBatchStatus.VALIDATED -> return next == RestBreakBatchStatus.PUBLISHED
            RestBreakBatchStatus.PUBLISHED -> {
                when (next) {
                    RestBreakBatchStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakBatchStatus) {
        if (!canMoveTo(next)) {
            error("rest break batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netTotal(): Double = round((quantity.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): RestBreakBatch {
        return RestBreakBatch(
            code = code,
            label = label,
            attempts = attempts,
            visible = visible,
            externalRef = externalRef,
            quantity = quantity,
            category = category,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rest break batch in status created. */
fun newRestBreakBatch(code: String, label: String): RestBreakBatch {
    return RestBreakBatch(
        code = code,
        label = label,
        attempts = 0,
        visible = false,
        externalRef = "",
        quantity = 0,
        category = "",
        approved = false,
        status = RestBreakBatchStatus.CREATED,
    )
}
