package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.text.normalize

/** Where a ev charge group is in its life cycle. */
enum class EvChargeGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A ev charge group. Its code is unique within a tenant. */
data class EvChargeGroup(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var country: String,
    var externalRef: String,
    var quantity: Int,
    var status: EvChargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeGroupStatus): Boolean {
        when (status) {
            EvChargeGroupStatus.OPEN -> {
                when (next) {
                    EvChargeGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            EvChargeGroupStatus.IN_REVIEW -> return next == EvChargeGroupStatus.REOPENED
            EvChargeGroupStatus.RESOLVED -> {
                when (next) {
                    EvChargeGroupStatus.REOPENED, EvChargeGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            EvChargeGroupStatus.REOPENED -> {
                when (next) {
                    EvChargeGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("ev charge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): EvChargeGroup {
        return EvChargeGroup(
            code = code,
            title = title,
            tags = tags,
            country = country,
            externalRef = externalRef,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a ev charge group in status open. */
fun newEvChargeGroup(code: String, title: String): EvChargeGroup {
    return EvChargeGroup(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        country = "",
        externalRef = "",
        quantity = 0,
        status = EvChargeGroupStatus.OPEN,
    )
}
