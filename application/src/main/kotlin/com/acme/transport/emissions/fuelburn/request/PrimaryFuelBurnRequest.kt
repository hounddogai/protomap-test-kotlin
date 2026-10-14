package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary fuel burn request is in its life cycle. */
enum class PrimaryFuelBurnRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary fuel burn request as the fuel burn team keeps it. */
data class PrimaryFuelBurnRequest(
    var code: String,
    var label: String,
    var score: Double,
    var priority: Int,
    var tags: MutableList<String>,
    var pinned: Boolean,
    var name: String,
    var routeCode: String,
    var checksum: String,
    var status: PrimaryFuelBurnRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFuelBurnRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFuelBurnRequestStatus): Boolean {
        when (status) {
            PrimaryFuelBurnRequestStatus.NEW -> return next == PrimaryFuelBurnRequestStatus.RUNNING || next == PrimaryFuelBurnRequestStatus.FAILED
            PrimaryFuelBurnRequestStatus.QUEUED -> return next == PrimaryFuelBurnRequestStatus.RUNNING
            PrimaryFuelBurnRequestStatus.RUNNING -> {
                when (next) {
                    PrimaryFuelBurnRequestStatus.DONE -> return true
                }
                return false
            }
            PrimaryFuelBurnRequestStatus.DONE -> return next == PrimaryFuelBurnRequestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryFuelBurnRequestStatus) {
        if (!canMoveTo(next)) {
            error("primary fuel burn request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary fuel burn request"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round(score * priority.toDouble() * 100) / 100

    fun snapshot(): PrimaryFuelBurnRequest {
        return PrimaryFuelBurnRequest(
            code = code,
            label = label,
            score = score,
            priority = priority,
            tags = tags,
            pinned = pinned,
            name = name,
            routeCode = routeCode,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary fuel burn request in status new. */
fun newPrimaryFuelBurnRequest(code: String, label: String): PrimaryFuelBurnRequest {
    return PrimaryFuelBurnRequest(
        code = code,
        label = label,
        score = 0.0,
        priority = 0,
        tags = mutableListOf<String>(),
        pinned = false,
        name = "",
        routeCode = "",
        checksum = "",
        status = PrimaryFuelBurnRequestStatus.NEW,
    )
}
