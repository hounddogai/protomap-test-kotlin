package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity proposal is in its life cycle. */
enum class CapacityProposalStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capacity proposal. Its code is unique within a tenant. */
data class CapacityProposal(
    var code: String,
    var name: String,
    var pallets: Int,
    var expiresAt: Int,
    var score: Double,
    var checksum: String,
    var rate: Double,
    var ceiling: Double,
    var status: CapacityProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityProposalStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityProposalStatus): Boolean {
        when (status) {
            CapacityProposalStatus.NEW -> {
                when (next) {
                    CapacityProposalStatus.RUNNING, CapacityProposalStatus.FAILED -> return true
                }
                return false
            }
            CapacityProposalStatus.QUEUED -> return next == CapacityProposalStatus.DONE || next == CapacityProposalStatus.FAILED
            CapacityProposalStatus.RUNNING -> {
                when (next) {
                    CapacityProposalStatus.DONE, CapacityProposalStatus.FAILED -> return true
                }
                return false
            }
            CapacityProposalStatus.DONE -> return next == CapacityProposalStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CapacityProposalStatus) {
        if (!canMoveTo(next)) {
            error("capacity proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round(ceiling * rate * 100) / 100

    fun snapshot(): CapacityProposal {
        return CapacityProposal(
            code = code,
            name = name,
            pallets = pallets,
            expiresAt = expiresAt,
            score = score,
            checksum = checksum,
            rate = rate,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capacity proposal in status new. */
fun newCapacityProposal(code: String, name: String): CapacityProposal {
    return CapacityProposal(
        code = code,
        name = name,
        pallets = 0,
        expiresAt = 0,
        score = 0.0,
        checksum = "",
        rate = 0.0,
        ceiling = 0.0,
        status = CapacityProposalStatus.NEW,
    )
}
