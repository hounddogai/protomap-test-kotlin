package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending ev charge batch is in its life cycle. */
enum class PendingEvChargeBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending ev charge batch. Its code is unique within a tenant. */
data class PendingEvChargeBatch(
    var code: String,
    var label: String,
    var threshold: Double,
    var score: Double,
    var minimum: Double,
    var phone: String,
    var reviewer: String,
    var ceiling: Double,
    var tags: MutableList<String>,
    var weight: Double,
    var status: PendingEvChargeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingEvChargeBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingEvChargeBatchStatus): Boolean {
        when (status) {
            PendingEvChargeBatchStatus.REQUESTED -> {
                when (next) {
                    PendingEvChargeBatchStatus.REVOKED, PendingEvChargeBatchStatus.EXPIRED -> return true
                }
                return false
            }
            PendingEvChargeBatchStatus.GRANTED -> {
                when (next) {
                    PendingEvChargeBatchStatus.REVOKED, PendingEvChargeBatchStatus.EXPIRED -> return true
                }
                return false
            }
            PendingEvChargeBatchStatus.REVOKED -> {
                when (next) {
                    PendingEvChargeBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingEvChargeBatchStatus) {
        if (!canMoveTo(next)) {
            error("pending ev charge batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((weight - score) * 100) / 100

    fun snapshot(): PendingEvChargeBatch {
        return PendingEvChargeBatch(
            code = code,
            label = label,
            threshold = threshold,
            score = score,
            minimum = minimum,
            phone = phone,
            reviewer = reviewer,
            ceiling = ceiling,
            tags = tags,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending ev charge batch in status requested. */
fun newPendingEvChargeBatch(code: String, label: String): PendingEvChargeBatch {
    return PendingEvChargeBatch(
        code = code,
        label = label,
        threshold = 0.0,
        score = 0.0,
        minimum = 0.0,
        phone = "",
        reviewer = "",
        ceiling = 0.0,
        tags = mutableListOf<String>(),
        weight = 0.0,
        status = PendingEvChargeBatchStatus.REQUESTED,
    )
}
