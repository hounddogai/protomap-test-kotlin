package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary idling profile is in its life cycle. */
enum class PrimaryIdlingProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary idling profile. Its code is unique within a tenant. */
data class PrimaryIdlingProfile(
    var code: String,
    var title: String,
    var startsAt: Int,
    var currency: String,
    var attempts: Int,
    var capacity: Int,
    var status: PrimaryIdlingProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryIdlingProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryIdlingProfileStatus): Boolean {
        when (status) {
            PrimaryIdlingProfileStatus.NEW -> {
                when (next) {
                    PrimaryIdlingProfileStatus.FAILED -> return true
                }
                return false
            }
            PrimaryIdlingProfileStatus.QUEUED -> {
                when (next) {
                    PrimaryIdlingProfileStatus.RUNNING, PrimaryIdlingProfileStatus.DONE -> return true
                }
                return false
            }
            PrimaryIdlingProfileStatus.RUNNING -> {
                when (next) {
                    PrimaryIdlingProfileStatus.DONE -> return true
                }
                return false
            }
            PrimaryIdlingProfileStatus.DONE -> {
                when (next) {
                    PrimaryIdlingProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryIdlingProfileStatus) {
        if (!canMoveTo(next)) {
            error("primary idling profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round((capacity.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): PrimaryIdlingProfile {
        return PrimaryIdlingProfile(
            code = code,
            title = title,
            startsAt = startsAt,
            currency = currency,
            attempts = attempts,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary idling profile in status new. */
fun newPrimaryIdlingProfile(code: String, title: String): PrimaryIdlingProfile {
    return PrimaryIdlingProfile(
        code = code,
        title = title,
        startsAt = 0,
        currency = "",
        attempts = 0,
        capacity = 0,
        status = PrimaryIdlingProfileStatus.NEW,
    )
}
