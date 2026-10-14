package com.acme.transport.network.cutoff.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff allocation is in its life cycle. */
enum class CutoffAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One cutoff allocation, with what the planners need to act on it. */
data class CutoffAllocation(
    var code: String,
    var name: String,
    var createdAt: Int,
    var budget: Double,
    var attempts: Int,
    var active: Boolean,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var weight: Double,
    var status: CutoffAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffAllocationStatus): Boolean {
        when (status) {
            CutoffAllocationStatus.DRAFT -> {
                when (next) {
                    CutoffAllocationStatus.ACTIVE, CutoffAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            CutoffAllocationStatus.PENDING -> return next == CutoffAllocationStatus.ACTIVE
            CutoffAllocationStatus.ACTIVE -> {
                when (next) {
                    CutoffAllocationStatus.CLOSED -> return true
                }
                return false
            }
            CutoffAllocationStatus.SUSPENDED -> {
                when (next) {
                    CutoffAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffAllocationStatus) {
        if (!canMoveTo(next)) {
            error("cutoff allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round((budget - attempts.toDouble()) * 100) / 100

    fun snapshot(): CutoffAllocation {
        return CutoffAllocation(
            code = code,
            name = name,
            createdAt = createdAt,
            budget = budget,
            attempts = attempts,
            active = active,
            attributes = attributes,
            summary = summary,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cutoff allocation in status draft. */
fun newCutoffAllocation(code: String, name: String): CutoffAllocation {
    return CutoffAllocation(
        code = code,
        name = name,
        createdAt = 0,
        budget = 0.0,
        attempts = 0,
        active = false,
        attributes = mutableMapOf<String, String>(),
        summary = "",
        weight = 0.0,
        status = CutoffAllocationStatus.DRAFT,
    )
}
