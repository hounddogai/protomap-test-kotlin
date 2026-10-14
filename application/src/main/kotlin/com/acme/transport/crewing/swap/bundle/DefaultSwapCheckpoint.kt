package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default swap checkpoint is in its life cycle. */
enum class DefaultSwapCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default swap checkpoint. Its code is unique within a tenant. */
data class DefaultSwapCheckpoint(
    var code: String,
    var title: String,
    var priority: Int,
    var externalRef: String,
    var minimum: Double,
    var dock: String,
    var source: String,
    var discount: Double,
    var status: DefaultSwapCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultSwapCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultSwapCheckpointStatus): Boolean {
        when (status) {
            DefaultSwapCheckpointStatus.PLANNED -> return next == DefaultSwapCheckpointStatus.CANCELLED
            DefaultSwapCheckpointStatus.SCHEDULED -> return next == DefaultSwapCheckpointStatus.IN_PROGRESS || next == DefaultSwapCheckpointStatus.CANCELLED
            DefaultSwapCheckpointStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultSwapCheckpointStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultSwapCheckpointStatus.COMPLETED -> return next == DefaultSwapCheckpointStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultSwapCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("default swap checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((minimum + priority.toDouble()) * 100) / 100

    fun snapshot(): DefaultSwapCheckpoint {
        return DefaultSwapCheckpoint(
            code = code,
            title = title,
            priority = priority,
            externalRef = externalRef,
            minimum = minimum,
            dock = dock,
            source = source,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default swap checkpoint in status planned. */
fun newDefaultSwapCheckpoint(code: String, title: String): DefaultSwapCheckpoint {
    return DefaultSwapCheckpoint(
        code = code,
        title = title,
        priority = 0,
        externalRef = "",
        minimum = 0.0,
        dock = "",
        source = "",
        discount = 0.0,
        status = DefaultSwapCheckpointStatus.PLANNED,
    )
}
