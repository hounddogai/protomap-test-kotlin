package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched carbon report policy is in its life cycle. */
enum class BatchedCarbonReportPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched carbon report policy. Its code is unique within a tenant. */
data class BatchedCarbonReportPolicy(
    var code: String,
    var label: String,
    var ownerId: String,
    var revision: Int,
    var retries: Int,
    var active: Boolean,
    var capacity: Int,
    var margin: Double,
    var status: BatchedCarbonReportPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCarbonReportPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCarbonReportPolicyStatus): Boolean {
        when (status) {
            BatchedCarbonReportPolicyStatus.PROPOSED -> {
                when (next) {
                    BatchedCarbonReportPolicyStatus.APPROVED, BatchedCarbonReportPolicyStatus.REJECTED -> return true
                }
                return false
            }
            BatchedCarbonReportPolicyStatus.APPROVED -> {
                when (next) {
                    BatchedCarbonReportPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedCarbonReportPolicyStatus.REJECTED -> {
                when (next) {
                    BatchedCarbonReportPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCarbonReportPolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched carbon report policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round((capacity.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): BatchedCarbonReportPolicy {
        return BatchedCarbonReportPolicy(
            code = code,
            label = label,
            ownerId = ownerId,
            revision = revision,
            retries = retries,
            active = active,
            capacity = capacity,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched carbon report policy in status proposed. */
fun newBatchedCarbonReportPolicy(code: String, label: String): BatchedCarbonReportPolicy {
    return BatchedCarbonReportPolicy(
        code = code,
        label = label,
        ownerId = "",
        revision = 0,
        retries = 0,
        active = false,
        capacity = 0,
        margin = 0.0,
        status = BatchedCarbonReportPolicyStatus.PROPOSED,
    )
}
