package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.text.normalize

/** Where a declaration checkpoint is in its life cycle. */
enum class DeclarationCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A declaration checkpoint. Its code is unique within a tenant. */
data class DeclarationCheckpoint(
    var code: String,
    var title: String,
    var source: String,
    var checksum: String,
    var carrier: String,
    var score: Double,
    var status: DeclarationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationCheckpointStatus): Boolean {
        when (status) {
            DeclarationCheckpointStatus.CREATED -> {
                when (next) {
                    DeclarationCheckpointStatus.VALIDATED -> return true
                }
                return false
            }
            DeclarationCheckpointStatus.VALIDATED -> return next == DeclarationCheckpointStatus.PUBLISHED
            DeclarationCheckpointStatus.PUBLISHED -> return next == DeclarationCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DeclarationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("declaration checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = score

    fun snapshot(): DeclarationCheckpoint {
        return DeclarationCheckpoint(
            code = code,
            title = title,
            source = source,
            checksum = checksum,
            carrier = carrier,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a declaration checkpoint in status created. */
fun newDeclarationCheckpoint(code: String, title: String): DeclarationCheckpoint {
    return DeclarationCheckpoint(
        code = code,
        title = title,
        source = "",
        checksum = "",
        carrier = "",
        score = 0.0,
        status = DeclarationCheckpointStatus.CREATED,
    )
}
