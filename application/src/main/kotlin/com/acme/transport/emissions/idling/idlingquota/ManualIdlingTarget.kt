package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.kit.text.normalize

/** Where a manual idling target is in its life cycle. */
enum class ManualIdlingTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual idling target, with what the planners need to act on it. */
data class ManualIdlingTarget(
    var code: String,
    var title: String,
    var reviewer: String,
    var visible: Boolean,
    var pallets: Int,
    var status: ManualIdlingTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIdlingTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIdlingTargetStatus): Boolean {
        when (status) {
            ManualIdlingTargetStatus.DRAFT -> {
                when (next) {
                    ManualIdlingTargetStatus.PENDING, ManualIdlingTargetStatus.CLOSED -> return true
                }
                return false
            }
            ManualIdlingTargetStatus.PENDING -> {
                when (next) {
                    ManualIdlingTargetStatus.SUSPENDED, ManualIdlingTargetStatus.CLOSED -> return true
                }
                return false
            }
            ManualIdlingTargetStatus.ACTIVE -> {
                when (next) {
                    ManualIdlingTargetStatus.CLOSED -> return true
                }
                return false
            }
            ManualIdlingTargetStatus.SUSPENDED -> {
                when (next) {
                    ManualIdlingTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualIdlingTargetStatus) {
        if (!canMoveTo(next)) {
            error("manual idling target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): ManualIdlingTarget {
        return ManualIdlingTarget(
            code = code,
            title = title,
            reviewer = reviewer,
            visible = visible,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual idling target in status draft. */
fun newManualIdlingTarget(code: String, title: String): ManualIdlingTarget {
    return ManualIdlingTarget(
        code = code,
        title = title,
        reviewer = "",
        visible = false,
        pallets = 0,
        status = ManualIdlingTargetStatus.DRAFT,
    )
}
