package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul request is in its life cycle. */
enum class BackhaulRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A backhaul request as the backhaul team keeps it. */
data class BackhaulRequest(
    var code: String,
    var label: String,
    var capacity: Int,
    var channel: String,
    var loadKg: Double,
    var status: BackhaulRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulRequestStatus): Boolean {
        when (status) {
            BackhaulRequestStatus.CREATED -> {
                when (next) {
                    BackhaulRequestStatus.PUBLISHED -> return true
                }
                return false
            }
            BackhaulRequestStatus.VALIDATED -> {
                when (next) {
                    BackhaulRequestStatus.PUBLISHED -> return true
                }
                return false
            }
            BackhaulRequestStatus.PUBLISHED -> {
                when (next) {
                    BackhaulRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulRequestStatus) {
        if (!canMoveTo(next)) {
            error("backhaul request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedTotal(): Double = round((capacity.toDouble() + loadKg) * 100) / 100

    fun snapshot(): BackhaulRequest {
        return BackhaulRequest(
            code = code,
            label = label,
            capacity = capacity,
            channel = channel,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a backhaul request in status created. */
fun newBackhaulRequest(code: String, label: String): BackhaulRequest {
    return BackhaulRequest(
        code = code,
        label = label,
        capacity = 0,
        channel = "",
        loadKg = 0.0,
        status = BackhaulRequestStatus.CREATED,
    )
}
