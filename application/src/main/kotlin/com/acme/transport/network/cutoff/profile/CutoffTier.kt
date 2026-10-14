package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff tier is in its life cycle. */
enum class CutoffTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff tier. Its code is unique within a tenant. */
data class CutoffTier(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var score: Double,
    var name: String,
    var status: CutoffTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffTierStatus): Boolean {
        when (status) {
            CutoffTierStatus.NEW -> {
                when (next) {
                    CutoffTierStatus.QUEUED -> return true
                }
                return false
            }
            CutoffTierStatus.QUEUED -> {
                when (next) {
                    CutoffTierStatus.DONE -> return true
                }
                return false
            }
            CutoffTierStatus.RUNNING -> {
                when (next) {
                    CutoffTierStatus.DONE -> return true
                }
                return false
            }
            CutoffTierStatus.DONE -> {
                when (next) {
                    CutoffTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffTierStatus) {
        if (!canMoveTo(next)) {
            error("cutoff tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((durationMinutes.toDouble() + score) * 100) / 100

    fun snapshot(): CutoffTier {
        return CutoffTier(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            score = score,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cutoff tier in status new. */
fun newCutoffTier(code: String, label: String): CutoffTier {
    return CutoffTier(
        code = code,
        label = label,
        durationMinutes = 0,
        score = 0.0,
        name = "",
        status = CutoffTierStatus.NEW,
    )
}
