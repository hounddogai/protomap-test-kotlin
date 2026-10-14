package com.acme.transport.customs.broker.digest

import com.acme.transport.kit.text.normalize

// The manual broker digest records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a manual broker digest is in its life cycle. */
enum class ManualBrokerDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual broker digest as the broker team keeps it. */
data class ManualBrokerDigest(
    var code: String,
    var label: String,
    var title: String,
    var checksum: String,
    var active: Boolean,
    var amount: Double,
    var status: ManualBrokerDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBrokerDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBrokerDigestStatus): Boolean {
        when (status) {
            ManualBrokerDigestStatus.DRAFT -> return next == ManualBrokerDigestStatus.SUSPENDED || next == ManualBrokerDigestStatus.CLOSED
            ManualBrokerDigestStatus.PENDING -> {
                when (next) {
                    ManualBrokerDigestStatus.ACTIVE, ManualBrokerDigestStatus.CLOSED -> return true
                }
                return false
            }
            ManualBrokerDigestStatus.ACTIVE -> {
                when (next) {
                    ManualBrokerDigestStatus.SUSPENDED, ManualBrokerDigestStatus.CLOSED -> return true
                }
                return false
            }
            ManualBrokerDigestStatus.SUSPENDED -> {
                when (next) {
                    ManualBrokerDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualBrokerDigestStatus) {
        if (!canMoveTo(next)) {
            error("manual broker digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): ManualBrokerDigest {
        return ManualBrokerDigest(
            code = code,
            label = label,
            title = title,
            checksum = checksum,
            active = active,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual broker digest in status draft. */
fun newManualBrokerDigest(code: String, label: String): ManualBrokerDigest {
    return ManualBrokerDigest(
        code = code,
        label = label,
        title = "",
        checksum = "",
        active = false,
        amount = 0.0,
        status = ManualBrokerDigestStatus.DRAFT,
    )
}
