package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched declaration digest is in its life cycle. */
enum class BatchedDeclarationDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched declaration digest. Its code is unique within a tenant. */
data class BatchedDeclarationDigest(
    var code: String,
    var label: String,
    var margin: Double,
    var priority: Int,
    var locale: String,
    var description: String,
    var summary: String,
    var limit: Int,
    var status: BatchedDeclarationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDeclarationDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDeclarationDigestStatus): Boolean {
        when (status) {
            BatchedDeclarationDigestStatus.REQUESTED -> return next == BatchedDeclarationDigestStatus.GRANTED || next == BatchedDeclarationDigestStatus.EXPIRED
            BatchedDeclarationDigestStatus.GRANTED -> {
                when (next) {
                    BatchedDeclarationDigestStatus.REVOKED, BatchedDeclarationDigestStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedDeclarationDigestStatus.REVOKED -> return next == BatchedDeclarationDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedDeclarationDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched declaration digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedExposure(): Double = round((priority.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): BatchedDeclarationDigest {
        return BatchedDeclarationDigest(
            code = code,
            label = label,
            margin = margin,
            priority = priority,
            locale = locale,
            description = description,
            summary = summary,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched declaration digest in status requested. */
fun newBatchedDeclarationDigest(code: String, label: String): BatchedDeclarationDigest {
    return BatchedDeclarationDigest(
        code = code,
        label = label,
        margin = 0.0,
        priority = 0,
        locale = "",
        description = "",
        summary = "",
        limit = 0,
        status = BatchedDeclarationDigestStatus.REQUESTED,
    )
}
