package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched lease return policy is in its life cycle. */
enum class BatchedLeaseReturnPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched lease return policy. Its code is unique within a tenant. */
data class BatchedLeaseReturnPolicy(
    var code: String,
    var label: String,
    var trailerId: String,
    var quantity: Int,
    var priority: Int,
    var revision: Int,
    var locale: String,
    var archived: Boolean,
    var active: Boolean,
    var category: String,
    var contactEmail: String,
    var status: BatchedLeaseReturnPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLeaseReturnPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLeaseReturnPolicyStatus): Boolean {
        when (status) {
            BatchedLeaseReturnPolicyStatus.DRAFT -> {
                when (next) {
                    BatchedLeaseReturnPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedLeaseReturnPolicyStatus.PENDING -> {
                when (next) {
                    BatchedLeaseReturnPolicyStatus.ACTIVE, BatchedLeaseReturnPolicyStatus.CLOSED -> return true
                }
                return false
            }
            BatchedLeaseReturnPolicyStatus.ACTIVE -> {
                when (next) {
                    BatchedLeaseReturnPolicyStatus.SUSPENDED, BatchedLeaseReturnPolicyStatus.CLOSED -> return true
                }
                return false
            }
            BatchedLeaseReturnPolicyStatus.SUSPENDED -> {
                when (next) {
                    BatchedLeaseReturnPolicyStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedLeaseReturnPolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched lease return policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"batched lease return policy"} $code"

    fun netLoad(): Double = round((revision.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): BatchedLeaseReturnPolicy {
        return BatchedLeaseReturnPolicy(
            code = code,
            label = label,
            trailerId = trailerId,
            quantity = quantity,
            priority = priority,
            revision = revision,
            locale = locale,
            archived = archived,
            active = active,
            category = category,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched lease return policy in status draft. */
fun newBatchedLeaseReturnPolicy(code: String, label: String): BatchedLeaseReturnPolicy {
    return BatchedLeaseReturnPolicy(
        code = code,
        label = label,
        trailerId = "",
        quantity = 0,
        priority = 0,
        revision = 0,
        locale = "",
        archived = false,
        active = false,
        category = "",
        contactEmail = "",
        status = BatchedLeaseReturnPolicyStatus.DRAFT,
    )
}
