package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop profile is in its life cycle. */
enum class DropProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One drop profile, with what the planners need to act on it. */
data class DropProfile(
    var code: String,
    var label: String,
    var quantity: Int,
    var active: Boolean,
    var minimum: Double,
    var currency: String,
    var score: Double,
    var revision: Int,
    var routeCode: String,
    var status: DropProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropProfileStatus): Boolean {
        when (status) {
            DropProfileStatus.NEW -> return next == DropProfileStatus.QUEUED
            DropProfileStatus.QUEUED -> {
                when (next) {
                    DropProfileStatus.RUNNING -> return true
                }
                return false
            }
            DropProfileStatus.RUNNING -> return next == DropProfileStatus.FAILED
            DropProfileStatus.DONE -> {
                when (next) {
                    DropProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropProfileStatus) {
        if (!canMoveTo(next)) {
            error("drop profile $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"drop profile"} $code"

    fun effectiveExposure(): Double = round(revision.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): DropProfile {
        return DropProfile(
            code = code,
            label = label,
            quantity = quantity,
            active = active,
            minimum = minimum,
            currency = currency,
            score = score,
            revision = revision,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a drop profile in status new. */
fun newDropProfile(code: String, label: String): DropProfile {
    return DropProfile(
        code = code,
        label = label,
        quantity = 0,
        active = false,
        minimum = 0.0,
        currency = "",
        score = 0.0,
        revision = 0,
        routeCode = "",
        status = DropProfileStatus.NEW,
    )
}
