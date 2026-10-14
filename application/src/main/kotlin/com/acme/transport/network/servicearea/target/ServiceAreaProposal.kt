package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area proposal is in its life cycle. */
enum class ServiceAreaProposalStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A service area proposal. Its code is unique within a tenant. */
data class ServiceAreaProposal(
    var code: String,
    var label: String,
    var depot: String,
    var expiresAt: Int,
    var visible: Boolean,
    var capacity: Int,
    var minimum: Double,
    var batchSize: Int,
    var sequence: Int,
    var shiftCode: String,
    var status: ServiceAreaProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaProposalStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaProposalStatus): Boolean {
        when (status) {
            ServiceAreaProposalStatus.NEW -> {
                when (next) {
                    ServiceAreaProposalStatus.FAILED -> return true
                }
                return false
            }
            ServiceAreaProposalStatus.QUEUED -> return next == ServiceAreaProposalStatus.RUNNING || next == ServiceAreaProposalStatus.DONE
            ServiceAreaProposalStatus.RUNNING -> {
                when (next) {
                    ServiceAreaProposalStatus.DONE -> return true
                }
                return false
            }
            ServiceAreaProposalStatus.DONE -> return next == ServiceAreaProposalStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ServiceAreaProposalStatus) {
        if (!canMoveTo(next)) {
            error("service area proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round(minimum * sequence.toDouble() * 100) / 100

    fun snapshot(): ServiceAreaProposal {
        return ServiceAreaProposal(
            code = code,
            label = label,
            depot = depot,
            expiresAt = expiresAt,
            visible = visible,
            capacity = capacity,
            minimum = minimum,
            batchSize = batchSize,
            sequence = sequence,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a service area proposal in status new. */
fun newServiceAreaProposal(code: String, label: String): ServiceAreaProposal {
    return ServiceAreaProposal(
        code = code,
        label = label,
        depot = "",
        expiresAt = 0,
        visible = false,
        capacity = 0,
        minimum = 0.0,
        batchSize = 0,
        sequence = 0,
        shiftCode = "",
        status = ServiceAreaProposalStatus.NEW,
    )
}
