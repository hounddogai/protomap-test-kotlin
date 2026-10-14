package com.acme.transport.customs.declaration.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default declaration allocation is in its life cycle. */
enum class DefaultDeclarationAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default declaration allocation as the declaration team keeps it. */
data class DefaultDeclarationAllocation(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var locale: String,
    var ratio: Double,
    var currency: String,
    var minimum: Double,
    var approved: Boolean,
    var status: DefaultDeclarationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDeclarationAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDeclarationAllocationStatus): Boolean {
        when (status) {
            DefaultDeclarationAllocationStatus.PROPOSED -> return next == DefaultDeclarationAllocationStatus.REJECTED || next == DefaultDeclarationAllocationStatus.WITHDRAWN
            DefaultDeclarationAllocationStatus.APPROVED -> {
                when (next) {
                    DefaultDeclarationAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultDeclarationAllocationStatus.REJECTED -> {
                when (next) {
                    DefaultDeclarationAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDeclarationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("default declaration allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((ratio - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefaultDeclarationAllocation {
        return DefaultDeclarationAllocation(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            locale = locale,
            ratio = ratio,
            currency = currency,
            minimum = minimum,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default declaration allocation in status proposed. */
fun newDefaultDeclarationAllocation(code: String, label: String): DefaultDeclarationAllocation {
    return DefaultDeclarationAllocation(
        code = code,
        label = label,
        etaMinutes = 0,
        locale = "",
        ratio = 0.0,
        currency = "",
        minimum = 0.0,
        approved = false,
        status = DefaultDeclarationAllocationStatus.PROPOSED,
    )
}
