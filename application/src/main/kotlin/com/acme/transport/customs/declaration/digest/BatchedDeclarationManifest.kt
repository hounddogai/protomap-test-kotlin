package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched declaration manifest is in its life cycle. */
enum class BatchedDeclarationManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched declaration manifest as the declaration team keeps it. */
data class BatchedDeclarationManifest(
    var code: String,
    var name: String,
    var channel: String,
    var score: Double,
    var dock: String,
    var contactEmail: String,
    var externalRef: String,
    var windowDays: Int,
    var summary: String,
    var active: Boolean,
    var createdAt: Int,
    var status: BatchedDeclarationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDeclarationManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDeclarationManifestStatus): Boolean {
        when (status) {
            BatchedDeclarationManifestStatus.IDLE -> return next == BatchedDeclarationManifestStatus.WARMING || next == BatchedDeclarationManifestStatus.STOPPED
            BatchedDeclarationManifestStatus.WARMING -> return next == BatchedDeclarationManifestStatus.READY || next == BatchedDeclarationManifestStatus.STOPPED
            BatchedDeclarationManifestStatus.READY -> {
                when (next) {
                    BatchedDeclarationManifestStatus.DRAINING -> return true
                }
                return false
            }
            BatchedDeclarationManifestStatus.DRAINING -> {
                when (next) {
                    BatchedDeclarationManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDeclarationManifestStatus) {
        if (!canMoveTo(next)) {
            error("batched declaration manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round((windowDays.toDouble() + score) * 100) / 100

    fun snapshot(): BatchedDeclarationManifest {
        return BatchedDeclarationManifest(
            code = code,
            name = name,
            channel = channel,
            score = score,
            dock = dock,
            contactEmail = contactEmail,
            externalRef = externalRef,
            windowDays = windowDays,
            summary = summary,
            active = active,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched declaration manifest in status idle. */
fun newBatchedDeclarationManifest(code: String, name: String): BatchedDeclarationManifest {
    return BatchedDeclarationManifest(
        code = code,
        name = name,
        channel = "",
        score = 0.0,
        dock = "",
        contactEmail = "",
        externalRef = "",
        windowDays = 0,
        summary = "",
        active = false,
        createdAt = 0,
        status = BatchedDeclarationManifestStatus.IDLE,
    )
}
