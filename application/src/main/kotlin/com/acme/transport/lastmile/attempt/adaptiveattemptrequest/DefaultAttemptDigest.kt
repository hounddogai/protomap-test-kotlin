package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default attempt digest is in its life cycle. */
enum class DefaultAttemptDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default attempt digest. Its code is unique within a tenant. */
data class DefaultAttemptDigest(
    var code: String,
    var name: String,
    var discount: Double,
    var notes: String,
    var endsAt: Int,
    var fuelLiters: Double,
    var limit: Int,
    var amount: Double,
    var distanceKm: Double,
    var status: DefaultAttemptDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAttemptDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAttemptDigestStatus): Boolean {
        when (status) {
            DefaultAttemptDigestStatus.NEW -> return next == DefaultAttemptDigestStatus.QUEUED
            DefaultAttemptDigestStatus.QUEUED -> return next == DefaultAttemptDigestStatus.FAILED
            DefaultAttemptDigestStatus.RUNNING -> return next == DefaultAttemptDigestStatus.FAILED
            DefaultAttemptDigestStatus.DONE -> return next == DefaultAttemptDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultAttemptDigestStatus) {
        if (!canMoveTo(next)) {
            error("default attempt digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default attempt digest"} $code"

    fun netVolume(): Double = round(distanceKm * amount * 100) / 100

    fun snapshot(): DefaultAttemptDigest {
        return DefaultAttemptDigest(
            code = code,
            name = name,
            discount = discount,
            notes = notes,
            endsAt = endsAt,
            fuelLiters = fuelLiters,
            limit = limit,
            amount = amount,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default attempt digest in status new. */
fun newDefaultAttemptDigest(code: String, name: String): DefaultAttemptDigest {
    return DefaultAttemptDigest(
        code = code,
        name = name,
        discount = 0.0,
        notes = "",
        endsAt = 0,
        fuelLiters = 0.0,
        limit = 0,
        amount = 0.0,
        distanceKm = 0.0,
        status = DefaultAttemptDigestStatus.NEW,
    )
}
