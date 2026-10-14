package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery exception records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery exception is in its life cycle. */
enum class ProofOfDeliveryExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One proof of delivery exception, with what the planners need to act on it. */
data class ProofOfDeliveryException(
    var code: String,
    var name: String,
    var checksum: String,
    var ceiling: Double,
    var batchSize: Int,
    var externalRef: String,
    var version: Int,
    var floorValue: Double,
    var category: String,
    var channel: String,
    var dock: String,
    var status: ProofOfDeliveryExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryExceptionStatus): Boolean {
        when (status) {
            ProofOfDeliveryExceptionStatus.NEW -> {
                when (next) {
                    ProofOfDeliveryExceptionStatus.FAILED -> return true
                }
                return false
            }
            ProofOfDeliveryExceptionStatus.QUEUED -> return next == ProofOfDeliveryExceptionStatus.DONE
            ProofOfDeliveryExceptionStatus.RUNNING -> {
                when (next) {
                    ProofOfDeliveryExceptionStatus.FAILED -> return true
                }
                return false
            }
            ProofOfDeliveryExceptionStatus.DONE -> return next == ProofOfDeliveryExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryExceptionStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery exception $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"proof of delivery exception"} $code"

    fun adjustedVolume(): Double = round((version.toDouble() - ceiling) * 100) / 100

    fun snapshot(): ProofOfDeliveryException {
        return ProofOfDeliveryException(
            code = code,
            name = name,
            checksum = checksum,
            ceiling = ceiling,
            batchSize = batchSize,
            externalRef = externalRef,
            version = version,
            floorValue = floorValue,
            category = category,
            channel = channel,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a proof of delivery exception in status new. */
fun newProofOfDeliveryException(code: String, name: String): ProofOfDeliveryException {
    return ProofOfDeliveryException(
        code = code,
        name = name,
        checksum = "",
        ceiling = 0.0,
        batchSize = 0,
        externalRef = "",
        version = 0,
        floorValue = 0.0,
        category = "",
        channel = "",
        dock = "",
        status = ProofOfDeliveryExceptionStatus.NEW,
    )
}
