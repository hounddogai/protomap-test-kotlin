package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.text.normalize

/** Where a global declaration allocation is in its life cycle. */
enum class GlobalDeclarationAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global declaration allocation. Its code is unique within a tenant. */
data class GlobalDeclarationAllocation(
    var code: String,
    var title: String,
    var lane: String,
    var region: String,
    var active: Boolean,
    var rate: Double,
    var status: GlobalDeclarationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDeclarationAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDeclarationAllocationStatus): Boolean {
        when (status) {
            GlobalDeclarationAllocationStatus.IDLE -> return next == GlobalDeclarationAllocationStatus.WARMING
            GlobalDeclarationAllocationStatus.WARMING -> {
                when (next) {
                    GlobalDeclarationAllocationStatus.READY, GlobalDeclarationAllocationStatus.STOPPED -> return true
                }
                return false
            }
            GlobalDeclarationAllocationStatus.READY -> {
                when (next) {
                    GlobalDeclarationAllocationStatus.DRAINING -> return true
                }
                return false
            }
            GlobalDeclarationAllocationStatus.DRAINING -> {
                when (next) {
                    GlobalDeclarationAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalDeclarationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("global declaration allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = rate

    fun snapshot(): GlobalDeclarationAllocation {
        return GlobalDeclarationAllocation(
            code = code,
            title = title,
            lane = lane,
            region = region,
            active = active,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global declaration allocation in status idle. */
fun newGlobalDeclarationAllocation(code: String, title: String): GlobalDeclarationAllocation {
    return GlobalDeclarationAllocation(
        code = code,
        title = title,
        lane = "",
        region = "",
        active = false,
        rate = 0.0,
        status = GlobalDeclarationAllocationStatus.IDLE,
    )
}
