package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered accessorial batch is in its life cycle. */
enum class TieredAccessorialBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered accessorial batch and its current status. */
data class TieredAccessorialBatch(
    var code: String,
    var title: String,
    var lane: String,
    var sequence: Int,
    var attributes: MutableMap<String, String>,
    var dock: String,
    var revision: Int,
    var version: Int,
    var amount: Double,
    var status: TieredAccessorialBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAccessorialBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAccessorialBatchStatus): Boolean {
        when (status) {
            TieredAccessorialBatchStatus.OPEN -> {
                when (next) {
                    TieredAccessorialBatchStatus.REOPENED -> return true
                }
                return false
            }
            TieredAccessorialBatchStatus.IN_REVIEW -> return next == TieredAccessorialBatchStatus.REOPENED || next == TieredAccessorialBatchStatus.ARCHIVED
            TieredAccessorialBatchStatus.RESOLVED -> {
                when (next) {
                    TieredAccessorialBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredAccessorialBatchStatus.REOPENED -> {
                when (next) {
                    TieredAccessorialBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredAccessorialBatchStatus) {
        if (!canMoveTo(next)) {
            error("tiered accessorial batch $code cannot move from $status to $next")
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

    fun weightedLoad(): Double = round((sequence.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): TieredAccessorialBatch {
        return TieredAccessorialBatch(
            code = code,
            title = title,
            lane = lane,
            sequence = sequence,
            attributes = attributes,
            dock = dock,
            revision = revision,
            version = version,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered accessorial batch in status open. */
fun newTieredAccessorialBatch(code: String, title: String): TieredAccessorialBatch {
    return TieredAccessorialBatch(
        code = code,
        title = title,
        lane = "",
        sequence = 0,
        attributes = mutableMapOf<String, String>(),
        dock = "",
        revision = 0,
        version = 0,
        amount = 0.0,
        status = TieredAccessorialBatchStatus.OPEN,
    )
}
