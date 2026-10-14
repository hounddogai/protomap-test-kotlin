package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration draft is in its life cycle. */
enum class DeclarationDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A declaration draft. Its code is unique within a tenant. */
data class DeclarationDraft(
    var code: String,
    var label: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var timezone: String,
    var segment: String,
    var createdAt: Int,
    var threshold: Double,
    var quantity: Int,
    var pallets: Int,
    var status: DeclarationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationDraftStatus): Boolean {
        when (status) {
            DeclarationDraftStatus.IDLE -> {
                when (next) {
                    DeclarationDraftStatus.DRAINING -> return true
                }
                return false
            }
            DeclarationDraftStatus.WARMING -> {
                when (next) {
                    DeclarationDraftStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationDraftStatus.READY -> return next == DeclarationDraftStatus.DRAINING || next == DeclarationDraftStatus.STOPPED
            DeclarationDraftStatus.DRAINING -> {
                when (next) {
                    DeclarationDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationDraftStatus) {
        if (!canMoveTo(next)) {
            error("declaration draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round(threshold * pallets.toDouble() * 100) / 100

    fun snapshot(): DeclarationDraft {
        return DeclarationDraft(
            code = code,
            label = label,
            sequence = sequence,
            tags = tags,
            timezone = timezone,
            segment = segment,
            createdAt = createdAt,
            threshold = threshold,
            quantity = quantity,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a declaration draft in status idle. */
fun newDeclarationDraft(code: String, label: String): DeclarationDraft {
    return DeclarationDraft(
        code = code,
        label = label,
        sequence = 0,
        tags = mutableListOf<String>(),
        timezone = "",
        segment = "",
        createdAt = 0,
        threshold = 0.0,
        quantity = 0,
        pallets = 0,
        status = DeclarationDraftStatus.IDLE,
    )
}
