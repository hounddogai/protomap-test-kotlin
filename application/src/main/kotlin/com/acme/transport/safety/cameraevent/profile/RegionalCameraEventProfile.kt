package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional camera event profile records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a regional camera event profile is in its life cycle. */
enum class RegionalCameraEventProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a regional camera event profile and its current status. */
data class RegionalCameraEventProfile(
    var code: String,
    var title: String,
    var phone: String,
    var ceiling: Double,
    var endsAt: Int,
    var expiresAt: Int,
    var status: RegionalCameraEventProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCameraEventProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCameraEventProfileStatus): Boolean {
        when (status) {
            RegionalCameraEventProfileStatus.NEW -> {
                when (next) {
                    RegionalCameraEventProfileStatus.RUNNING -> return true
                }
                return false
            }
            RegionalCameraEventProfileStatus.QUEUED -> return next == RegionalCameraEventProfileStatus.RUNNING || next == RegionalCameraEventProfileStatus.DONE
            RegionalCameraEventProfileStatus.RUNNING -> return next == RegionalCameraEventProfileStatus.DONE || next == RegionalCameraEventProfileStatus.FAILED
            RegionalCameraEventProfileStatus.DONE -> {
                when (next) {
                    RegionalCameraEventProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCameraEventProfileStatus) {
        if (!canMoveTo(next)) {
            error("regional camera event profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round(ceiling * endsAt.toDouble() * 100) / 100

    fun snapshot(): RegionalCameraEventProfile {
        return RegionalCameraEventProfile(
            code = code,
            title = title,
            phone = phone,
            ceiling = ceiling,
            endsAt = endsAt,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional camera event profile in status new. */
fun newRegionalCameraEventProfile(code: String, title: String): RegionalCameraEventProfile {
    return RegionalCameraEventProfile(
        code = code,
        title = title,
        phone = "",
        ceiling = 0.0,
        endsAt = 0,
        expiresAt = 0,
        status = RegionalCameraEventProfileStatus.NEW,
    )
}
