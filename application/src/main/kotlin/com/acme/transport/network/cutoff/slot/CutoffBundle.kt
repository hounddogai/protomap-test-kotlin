package com.acme.transport.network.cutoff.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff bundle is in its life cycle. */
enum class CutoffBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A cutoff bundle. Its code is unique within a tenant. */
data class CutoffBundle(
    var code: String,
    var label: String,
    var score: Double,
    var weight: Double,
    var active: Boolean,
    var retries: Int,
    var endsAt: Int,
    var priority: Int,
    var status: CutoffBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffBundleStatus): Boolean {
        when (status) {
            CutoffBundleStatus.PROPOSED -> {
                when (next) {
                    CutoffBundleStatus.APPROVED -> return true
                }
                return false
            }
            CutoffBundleStatus.APPROVED -> return next == CutoffBundleStatus.REJECTED || next == CutoffBundleStatus.WITHDRAWN
            CutoffBundleStatus.REJECTED -> {
                when (next) {
                    CutoffBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffBundleStatus) {
        if (!canMoveTo(next)) {
            error("cutoff bundle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"cutoff bundle"} $code"

    fun weightedValue(): Double = round((priority.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CutoffBundle {
        return CutoffBundle(
            code = code,
            label = label,
            score = score,
            weight = weight,
            active = active,
            retries = retries,
            endsAt = endsAt,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cutoff bundle in status proposed. */
fun newCutoffBundle(code: String, label: String): CutoffBundle {
    return CutoffBundle(
        code = code,
        label = label,
        score = 0.0,
        weight = 0.0,
        active = false,
        retries = 0,
        endsAt = 0,
        priority = 0,
        status = CutoffBundleStatus.PROPOSED,
    )
}
