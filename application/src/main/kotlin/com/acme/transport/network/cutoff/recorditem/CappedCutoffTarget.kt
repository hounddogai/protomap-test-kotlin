package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped cutoff target is in its life cycle. */
enum class CappedCutoffTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped cutoff target as the cutoff team keeps it. */
data class CappedCutoffTarget(
    var code: String,
    var label: String,
    var region: String,
    var source: String,
    var ownerId: String,
    var tags: MutableList<String>,
    var cost: Double,
    var ratio: Double,
    var minimum: Double,
    var durationMinutes: Int,
    var status: CappedCutoffTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCutoffTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCutoffTargetStatus): Boolean {
        when (status) {
            CappedCutoffTargetStatus.PLANNED -> return next == CappedCutoffTargetStatus.COMPLETED
            CappedCutoffTargetStatus.SCHEDULED -> return next == CappedCutoffTargetStatus.COMPLETED || next == CappedCutoffTargetStatus.CANCELLED
            CappedCutoffTargetStatus.IN_PROGRESS -> return next == CappedCutoffTargetStatus.COMPLETED || next == CappedCutoffTargetStatus.CANCELLED
            CappedCutoffTargetStatus.COMPLETED -> return next == CappedCutoffTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedCutoffTargetStatus) {
        if (!canMoveTo(next)) {
            error("capped cutoff target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((minimum - ratio) * 100) / 100

    fun snapshot(): CappedCutoffTarget {
        return CappedCutoffTarget(
            code = code,
            label = label,
            region = region,
            source = source,
            ownerId = ownerId,
            tags = tags,
            cost = cost,
            ratio = ratio,
            minimum = minimum,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped cutoff target in status planned. */
fun newCappedCutoffTarget(code: String, label: String): CappedCutoffTarget {
    return CappedCutoffTarget(
        code = code,
        label = label,
        region = "",
        source = "",
        ownerId = "",
        tags = mutableListOf<String>(),
        cost = 0.0,
        ratio = 0.0,
        minimum = 0.0,
        durationMinutes = 0,
        status = CappedCutoffTargetStatus.PLANNED,
    )
}
