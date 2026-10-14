package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker draft is in its life cycle. */
enum class BrokerDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a broker draft and its current status. */
data class BrokerDraft(
    var code: String,
    var label: String,
    var country: String,
    var version: Int,
    var carrier: String,
    var revision: Int,
    var weight: Double,
    var expiresAt: Int,
    var status: BrokerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerDraftStatus): Boolean {
        when (status) {
            BrokerDraftStatus.CREATED -> return next == BrokerDraftStatus.VALIDATED || next == BrokerDraftStatus.PUBLISHED
            BrokerDraftStatus.VALIDATED -> {
                when (next) {
                    BrokerDraftStatus.RETIRED -> return true
                }
                return false
            }
            BrokerDraftStatus.PUBLISHED -> return next == BrokerDraftStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BrokerDraftStatus) {
        if (!canMoveTo(next)) {
            error("broker draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"broker draft"} $code"

    fun adjustedLoad(): Double = round((revision.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): BrokerDraft {
        return BrokerDraft(
            code = code,
            label = label,
            country = country,
            version = version,
            carrier = carrier,
            revision = revision,
            weight = weight,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a broker draft in status created. */
fun newBrokerDraft(code: String, label: String): BrokerDraft {
    return BrokerDraft(
        code = code,
        label = label,
        country = "",
        version = 0,
        carrier = "",
        revision = 0,
        weight = 0.0,
        expiresAt = 0,
        status = BrokerDraftStatus.CREATED,
    )
}
