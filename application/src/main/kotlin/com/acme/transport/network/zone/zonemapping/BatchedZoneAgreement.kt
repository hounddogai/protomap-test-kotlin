package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched zone agreement is in its life cycle. */
enum class BatchedZoneAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a batched zone agreement and its current status. */
data class BatchedZoneAgreement(
    var code: String,
    var label: String,
    var amount: Double,
    var name: String,
    var source: String,
    var locale: String,
    var retries: Int,
    var pinned: Boolean,
    var limit: Int,
    var approved: Boolean,
    var score: Double,
    var status: BatchedZoneAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedZoneAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedZoneAgreementStatus): Boolean {
        when (status) {
            BatchedZoneAgreementStatus.DRAFT -> return next == BatchedZoneAgreementStatus.PENDING
            BatchedZoneAgreementStatus.PENDING -> {
                when (next) {
                    BatchedZoneAgreementStatus.ACTIVE -> return true
                }
                return false
            }
            BatchedZoneAgreementStatus.ACTIVE -> {
                when (next) {
                    BatchedZoneAgreementStatus.SUSPENDED, BatchedZoneAgreementStatus.CLOSED -> return true
                }
                return false
            }
            BatchedZoneAgreementStatus.SUSPENDED -> return next == BatchedZoneAgreementStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedZoneAgreementStatus) {
        if (!canMoveTo(next)) {
            error("batched zone agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round((limit.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): BatchedZoneAgreement {
        return BatchedZoneAgreement(
            code = code,
            label = label,
            amount = amount,
            name = name,
            source = source,
            locale = locale,
            retries = retries,
            pinned = pinned,
            limit = limit,
            approved = approved,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched zone agreement in status draft. */
fun newBatchedZoneAgreement(code: String, label: String): BatchedZoneAgreement {
    return BatchedZoneAgreement(
        code = code,
        label = label,
        amount = 0.0,
        name = "",
        source = "",
        locale = "",
        retries = 0,
        pinned = false,
        limit = 0,
        approved = false,
        score = 0.0,
        status = BatchedZoneAgreementStatus.DRAFT,
    )
}
