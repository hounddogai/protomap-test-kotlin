package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration allocation is in its life cycle. */
enum class DeclarationAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a declaration allocation and its current status. */
data class DeclarationAllocation(
    var code: String,
    var name: String,
    var margin: Double,
    var graceDays: Int,
    var durationMinutes: Int,
    var status: DeclarationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationAllocationStatus): Boolean {
        when (status) {
            DeclarationAllocationStatus.DRAFT -> {
                when (next) {
                    DeclarationAllocationStatus.PENDING, DeclarationAllocationStatus.ACTIVE -> return true
                }
                return false
            }
            DeclarationAllocationStatus.PENDING -> {
                when (next) {
                    DeclarationAllocationStatus.ACTIVE, DeclarationAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            DeclarationAllocationStatus.ACTIVE -> return next == DeclarationAllocationStatus.SUSPENDED || next == DeclarationAllocationStatus.CLOSED
            DeclarationAllocationStatus.SUSPENDED -> {
                when (next) {
                    DeclarationAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("declaration allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((durationMinutes.toDouble() - margin) * 100) / 100

    fun snapshot(): DeclarationAllocation {
        return DeclarationAllocation(
            code = code,
            name = name,
            margin = margin,
            graceDays = graceDays,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration allocation in status draft. */
fun newDeclarationAllocation(code: String, name: String): DeclarationAllocation {
    return DeclarationAllocation(
        code = code,
        name = name,
        margin = 0.0,
        graceDays = 0,
        durationMinutes = 0,
        status = DeclarationAllocationStatus.DRAFT,
    )
}
