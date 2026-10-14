package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.text.normalize

// The default camera event group records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a default camera event group is in its life cycle. */
enum class DefaultCameraEventGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default camera event group, with what the planners need to act on it. */
data class DefaultCameraEventGroup(
    var code: String,
    var label: String,
    var depot: String,
    var discount: Double,
    var pinned: Boolean,
    var status: DefaultCameraEventGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCameraEventGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCameraEventGroupStatus): Boolean {
        when (status) {
            DefaultCameraEventGroupStatus.REQUESTED -> {
                when (next) {
                    DefaultCameraEventGroupStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultCameraEventGroupStatus.GRANTED -> {
                when (next) {
                    DefaultCameraEventGroupStatus.REVOKED, DefaultCameraEventGroupStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultCameraEventGroupStatus.REVOKED -> {
                when (next) {
                    DefaultCameraEventGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCameraEventGroupStatus) {
        if (!canMoveTo(next)) {
            error("default camera event group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = discount

    fun snapshot(): DefaultCameraEventGroup {
        return DefaultCameraEventGroup(
            code = code,
            label = label,
            depot = depot,
            discount = discount,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default camera event group in status requested. */
fun newDefaultCameraEventGroup(code: String, label: String): DefaultCameraEventGroup {
    return DefaultCameraEventGroup(
        code = code,
        label = label,
        depot = "",
        discount = 0.0,
        pinned = false,
        status = DefaultCameraEventGroupStatus.REQUESTED,
    )
}
