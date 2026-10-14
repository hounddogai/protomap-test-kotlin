package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped swap template is in its life cycle. */
enum class CappedSwapTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped swap template as the swap team keeps it. */
data class CappedSwapTemplate(
    var code: String,
    var name: String,
    var weight: Double,
    var sequence: Int,
    var ratio: Double,
    var archived: Boolean,
    var status: CappedSwapTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedSwapTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedSwapTemplateStatus): Boolean {
        when (status) {
            CappedSwapTemplateStatus.PLANNED -> return next == CappedSwapTemplateStatus.IN_PROGRESS || next == CappedSwapTemplateStatus.CANCELLED
            CappedSwapTemplateStatus.SCHEDULED -> return next == CappedSwapTemplateStatus.COMPLETED
            CappedSwapTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    CappedSwapTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            CappedSwapTemplateStatus.COMPLETED -> return next == CappedSwapTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedSwapTemplateStatus) {
        if (!canMoveTo(next)) {
            error("capped swap template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((weight + ratio) * 100) / 100

    fun snapshot(): CappedSwapTemplate {
        return CappedSwapTemplate(
            code = code,
            name = name,
            weight = weight,
            sequence = sequence,
            ratio = ratio,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped swap template in status planned. */
fun newCappedSwapTemplate(code: String, name: String): CappedSwapTemplate {
    return CappedSwapTemplate(
        code = code,
        name = name,
        weight = 0.0,
        sequence = 0,
        ratio = 0.0,
        archived = false,
        status = CappedSwapTemplateStatus.PLANNED,
    )
}
