package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay record is in its life cycle. */
enum class RelayRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A relay record as the relay team keeps it. */
data class RelayRecord(
    var code: String,
    var title: String,
    var retries: Int,
    var tags: MutableList<String>,
    var threshold: Double,
    var attempts: Int,
    var limit: Int,
    var score: Double,
    var ratio: Double,
    var minimum: Double,
    var quantity: Int,
    var status: RelayRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayRecordStatus): Boolean {
        when (status) {
            RelayRecordStatus.DRAFT -> {
                when (next) {
                    RelayRecordStatus.ACTIVE, RelayRecordStatus.CLOSED -> return true
                }
                return false
            }
            RelayRecordStatus.PENDING -> return next == RelayRecordStatus.SUSPENDED || next == RelayRecordStatus.CLOSED
            RelayRecordStatus.ACTIVE -> return next == RelayRecordStatus.SUSPENDED
            RelayRecordStatus.SUSPENDED -> {
                when (next) {
                    RelayRecordStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayRecordStatus) {
        if (!canMoveTo(next)) {
            error("relay record $code cannot move from $status to $next")
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

    fun adjustedTotal(): Double = round((attempts.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RelayRecord {
        return RelayRecord(
            code = code,
            title = title,
            retries = retries,
            tags = tags,
            threshold = threshold,
            attempts = attempts,
            limit = limit,
            score = score,
            ratio = ratio,
            minimum = minimum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay record in status draft. */
fun newRelayRecord(code: String, title: String): RelayRecord {
    return RelayRecord(
        code = code,
        title = title,
        retries = 0,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        attempts = 0,
        limit = 0,
        score = 0.0,
        ratio = 0.0,
        minimum = 0.0,
        quantity = 0,
        status = RelayRecordStatus.DRAFT,
    )
}
