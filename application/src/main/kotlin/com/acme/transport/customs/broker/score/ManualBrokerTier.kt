package com.acme.transport.customs.broker.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual broker tier is in its life cycle. */
enum class ManualBrokerTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual broker tier, with what the planners need to act on it. */
data class ManualBrokerTier(
    var code: String,
    var label: String,
    var externalRef: String,
    var visible: Boolean,
    var endsAt: Int,
    var tags: MutableList<String>,
    var cost: Double,
    var summary: String,
    var status: ManualBrokerTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBrokerTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBrokerTierStatus): Boolean {
        when (status) {
            ManualBrokerTierStatus.PROPOSED -> {
                when (next) {
                    ManualBrokerTierStatus.APPROVED, ManualBrokerTierStatus.REJECTED -> return true
                }
                return false
            }
            ManualBrokerTierStatus.APPROVED -> {
                when (next) {
                    ManualBrokerTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualBrokerTierStatus.REJECTED -> {
                when (next) {
                    ManualBrokerTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualBrokerTierStatus) {
        if (!canMoveTo(next)) {
            error("manual broker tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"manual broker tier"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((cost - endsAt.toDouble()) * 100) / 100

    fun snapshot(): ManualBrokerTier {
        return ManualBrokerTier(
            code = code,
            label = label,
            externalRef = externalRef,
            visible = visible,
            endsAt = endsAt,
            tags = tags,
            cost = cost,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual broker tier in status proposed. */
fun newManualBrokerTier(code: String, label: String): ManualBrokerTier {
    return ManualBrokerTier(
        code = code,
        label = label,
        externalRef = "",
        visible = false,
        endsAt = 0,
        tags = mutableListOf<String>(),
        cost = 0.0,
        summary = "",
        status = ManualBrokerTierStatus.PROPOSED,
    )
}
