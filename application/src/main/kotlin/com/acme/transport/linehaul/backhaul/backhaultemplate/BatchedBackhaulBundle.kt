package com.acme.transport.linehaul.backhaul.backhaultemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched backhaul bundle is in its life cycle. */
enum class BatchedBackhaulBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched backhaul bundle, with what the planners need to act on it. */
data class BatchedBackhaulBundle(
    var code: String,
    var name: String,
    var maximum: Double,
    var externalRef: String,
    var shiftCode: String,
    var minimum: Double,
    var carrier: String,
    var visible: Boolean,
    var status: BatchedBackhaulBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedBackhaulBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedBackhaulBundleStatus): Boolean {
        when (status) {
            BatchedBackhaulBundleStatus.PROPOSED -> return next == BatchedBackhaulBundleStatus.APPROVED || next == BatchedBackhaulBundleStatus.WITHDRAWN
            BatchedBackhaulBundleStatus.APPROVED -> {
                when (next) {
                    BatchedBackhaulBundleStatus.REJECTED, BatchedBackhaulBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedBackhaulBundleStatus.REJECTED -> {
                when (next) {
                    BatchedBackhaulBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedBackhaulBundleStatus) {
        if (!canMoveTo(next)) {
            error("batched backhaul bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round((minimum - maximum) * 100) / 100

    fun snapshot(): BatchedBackhaulBundle {
        return BatchedBackhaulBundle(
            code = code,
            name = name,
            maximum = maximum,
            externalRef = externalRef,
            shiftCode = shiftCode,
            minimum = minimum,
            carrier = carrier,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched backhaul bundle in status proposed. */
fun newBatchedBackhaulBundle(code: String, name: String): BatchedBackhaulBundle {
    return BatchedBackhaulBundle(
        code = code,
        name = name,
        maximum = 0.0,
        externalRef = "",
        shiftCode = "",
        minimum = 0.0,
        carrier = "",
        visible = false,
        status = BatchedBackhaulBundleStatus.PROPOSED,
    )
}
