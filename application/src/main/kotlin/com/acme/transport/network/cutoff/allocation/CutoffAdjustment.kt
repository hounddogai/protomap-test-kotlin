package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff adjustment is in its life cycle. */
enum class CutoffAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One cutoff adjustment, with what the planners need to act on it. */
data class CutoffAdjustment(
    var code: String,
    var label: String,
    var quantity: Int,
    var tags: MutableList<String>,
    var name: String,
    var loadKg: Double,
    var notes: String,
    var ceiling: Double,
    var status: CutoffAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffAdjustmentStatus): Boolean {
        when (status) {
            CutoffAdjustmentStatus.NEW -> {
                when (next) {
                    CutoffAdjustmentStatus.QUEUED, CutoffAdjustmentStatus.DONE -> return true
                }
                return false
            }
            CutoffAdjustmentStatus.QUEUED -> {
                when (next) {
                    CutoffAdjustmentStatus.RUNNING, CutoffAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            CutoffAdjustmentStatus.RUNNING -> {
                when (next) {
                    CutoffAdjustmentStatus.DONE, CutoffAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            CutoffAdjustmentStatus.DONE -> {
                when (next) {
                    CutoffAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("cutoff adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cutoff adjustment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((ceiling + quantity.toDouble()) * 100) / 100

    fun snapshot(): CutoffAdjustment {
        return CutoffAdjustment(
            code = code,
            label = label,
            quantity = quantity,
            tags = tags,
            name = name,
            loadKg = loadKg,
            notes = notes,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cutoff adjustment in status new. */
fun newCutoffAdjustment(code: String, label: String): CutoffAdjustment {
    return CutoffAdjustment(
        code = code,
        label = label,
        quantity = 0,
        tags = mutableListOf<String>(),
        name = "",
        loadKg = 0.0,
        notes = "",
        ceiling = 0.0,
        status = CutoffAdjustmentStatus.NEW,
    )
}
