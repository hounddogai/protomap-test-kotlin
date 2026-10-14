package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery notice is in its life cycle. */
enum class ProofOfDeliveryNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a proof of delivery notice and its current status. */
data class ProofOfDeliveryNotice(
    var code: String,
    var name: String,
    var windowDays: Int,
    var contactEmail: String,
    var pallets: Int,
    var endsAt: Int,
    var cost: Double,
    var status: ProofOfDeliveryNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryNoticeStatus): Boolean {
        when (status) {
            ProofOfDeliveryNoticeStatus.NEW -> {
                when (next) {
                    ProofOfDeliveryNoticeStatus.QUEUED, ProofOfDeliveryNoticeStatus.FAILED -> return true
                }
                return false
            }
            ProofOfDeliveryNoticeStatus.QUEUED -> return next == ProofOfDeliveryNoticeStatus.FAILED
            ProofOfDeliveryNoticeStatus.RUNNING -> return next == ProofOfDeliveryNoticeStatus.DONE || next == ProofOfDeliveryNoticeStatus.FAILED
            ProofOfDeliveryNoticeStatus.DONE -> return next == ProofOfDeliveryNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryNoticeStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery notice $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round((cost - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryNotice {
        return ProofOfDeliveryNotice(
            code = code,
            name = name,
            windowDays = windowDays,
            contactEmail = contactEmail,
            pallets = pallets,
            endsAt = endsAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery notice in status new. */
fun newProofOfDeliveryNotice(code: String, name: String): ProofOfDeliveryNotice {
    return ProofOfDeliveryNotice(
        code = code,
        name = name,
        windowDays = 0,
        contactEmail = "",
        pallets = 0,
        endsAt = 0,
        cost = 0.0,
        status = ProofOfDeliveryNoticeStatus.NEW,
    )
}
