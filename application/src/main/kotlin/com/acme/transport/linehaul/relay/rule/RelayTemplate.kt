package com.acme.transport.linehaul.relay.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay template is in its life cycle. */
enum class RelayTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A relay template as the relay team keeps it. */
data class RelayTemplate(
    var code: String,
    var label: String,
    var threshold: Double,
    var weight: Double,
    var channel: String,
    var pinned: Boolean,
    var version: Int,
    var driverId: String,
    var status: RelayTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayTemplateStatus): Boolean {
        when (status) {
            RelayTemplateStatus.DRAFT -> {
                when (next) {
                    RelayTemplateStatus.PENDING -> return true
                }
                return false
            }
            RelayTemplateStatus.PENDING -> {
                when (next) {
                    RelayTemplateStatus.SUSPENDED, RelayTemplateStatus.CLOSED -> return true
                }
                return false
            }
            RelayTemplateStatus.ACTIVE -> return next == RelayTemplateStatus.SUSPENDED || next == RelayTemplateStatus.CLOSED
            RelayTemplateStatus.SUSPENDED -> return next == RelayTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RelayTemplateStatus) {
        if (!canMoveTo(next)) {
            error("relay template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((version.toDouble() - threshold) * 100) / 100

    fun snapshot(): RelayTemplate {
        return RelayTemplate(
            code = code,
            label = label,
            threshold = threshold,
            weight = weight,
            channel = channel,
            pinned = pinned,
            version = version,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a relay template in status draft. */
fun newRelayTemplate(code: String, label: String): RelayTemplate {
    return RelayTemplate(
        code = code,
        label = label,
        threshold = 0.0,
        weight = 0.0,
        channel = "",
        pinned = false,
        version = 0,
        driverId = "",
        status = RelayTemplateStatus.DRAFT,
    )
}
