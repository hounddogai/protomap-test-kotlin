package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard mapping is in its life cycle. */
enum class TrailerYardMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A trailer yard mapping. Its code is unique within a tenant. */
data class TrailerYardMapping(
    var code: String,
    var name: String,
    var minimum: Double,
    var ownerId: String,
    var etaMinutes: Int,
    var status: TrailerYardMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardMappingStatus): Boolean {
        when (status) {
            TrailerYardMappingStatus.NEW -> {
                when (next) {
                    TrailerYardMappingStatus.QUEUED, TrailerYardMappingStatus.RUNNING -> return true
                }
                return false
            }
            TrailerYardMappingStatus.QUEUED -> {
                when (next) {
                    TrailerYardMappingStatus.RUNNING, TrailerYardMappingStatus.FAILED -> return true
                }
                return false
            }
            TrailerYardMappingStatus.RUNNING -> return next == TrailerYardMappingStatus.DONE || next == TrailerYardMappingStatus.FAILED
            TrailerYardMappingStatus.DONE -> return next == TrailerYardMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TrailerYardMappingStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer yard mapping"} $code"

    fun adjustedTotal(): Double = round((minimum + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardMapping {
        return TrailerYardMapping(
            code = code,
            name = name,
            minimum = minimum,
            ownerId = ownerId,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer yard mapping in status new. */
fun newTrailerYardMapping(code: String, name: String): TrailerYardMapping {
    return TrailerYardMapping(
        code = code,
        name = name,
        minimum = 0.0,
        ownerId = "",
        etaMinutes = 0,
        status = TrailerYardMappingStatus.NEW,
    )
}
