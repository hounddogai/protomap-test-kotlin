package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery record is in its life cycle. */
enum class ProofOfDeliveryRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One proof of delivery record, with what the planners need to act on it. */
data class ProofOfDeliveryRecord(
    var code: String,
    var label: String,
    var revision: Int,
    var stopCount: Int,
    var startsAt: Int,
    var externalRef: String,
    var priority: Int,
    var contactEmail: String,
    var expiresAt: Int,
    var status: ProofOfDeliveryRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryRecordStatus): Boolean {
        when (status) {
            ProofOfDeliveryRecordStatus.NEW -> return next == ProofOfDeliveryRecordStatus.QUEUED || next == ProofOfDeliveryRecordStatus.DONE
            ProofOfDeliveryRecordStatus.QUEUED -> return next == ProofOfDeliveryRecordStatus.DONE
            ProofOfDeliveryRecordStatus.RUNNING -> return next == ProofOfDeliveryRecordStatus.DONE
            ProofOfDeliveryRecordStatus.DONE -> {
                when (next) {
                    ProofOfDeliveryRecordStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryRecordStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(expiresAt.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): ProofOfDeliveryRecord {
        return ProofOfDeliveryRecord(
            code = code,
            label = label,
            revision = revision,
            stopCount = stopCount,
            startsAt = startsAt,
            externalRef = externalRef,
            priority = priority,
            contactEmail = contactEmail,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery record in status new. */
fun newProofOfDeliveryRecord(code: String, label: String): ProofOfDeliveryRecord {
    return ProofOfDeliveryRecord(
        code = code,
        label = label,
        revision = 0,
        stopCount = 0,
        startsAt = 0,
        externalRef = "",
        priority = 0,
        contactEmail = "",
        expiresAt = 0,
        status = ProofOfDeliveryRecordStatus.NEW,
    )
}
