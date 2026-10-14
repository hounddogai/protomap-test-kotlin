package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched lease return rule is in its life cycle. */
enum class BatchedLeaseReturnRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched lease return rule. Its code is unique within a tenant. */
data class BatchedLeaseReturnRule(
    var code: String,
    var title: String,
    var name: String,
    var ratio: Double,
    var revision: Int,
    var status: BatchedLeaseReturnRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLeaseReturnRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLeaseReturnRuleStatus): Boolean {
        when (status) {
            BatchedLeaseReturnRuleStatus.IDLE -> {
                when (next) {
                    BatchedLeaseReturnRuleStatus.WARMING, BatchedLeaseReturnRuleStatus.DRAINING -> return true
                }
                return false
            }
            BatchedLeaseReturnRuleStatus.WARMING -> {
                when (next) {
                    BatchedLeaseReturnRuleStatus.READY -> return true
                }
                return false
            }
            BatchedLeaseReturnRuleStatus.READY -> {
                when (next) {
                    BatchedLeaseReturnRuleStatus.STOPPED -> return true
                }
                return false
            }
            BatchedLeaseReturnRuleStatus.DRAINING -> {
                when (next) {
                    BatchedLeaseReturnRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedLeaseReturnRuleStatus) {
        if (!canMoveTo(next)) {
            error("batched lease return rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"batched lease return rule"} $code"

    fun netTotal(): Double = round(ratio * revision.toDouble() * 100) / 100

    fun snapshot(): BatchedLeaseReturnRule {
        return BatchedLeaseReturnRule(
            code = code,
            title = title,
            name = name,
            ratio = ratio,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched lease return rule in status idle. */
fun newBatchedLeaseReturnRule(code: String, title: String): BatchedLeaseReturnRule {
    return BatchedLeaseReturnRule(
        code = code,
        title = title,
        name = "",
        ratio = 0.0,
        revision = 0,
        status = BatchedLeaseReturnRuleStatus.IDLE,
    )
}
