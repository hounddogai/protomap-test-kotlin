package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender target is in its life cycle. */
enum class TenderTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tender target and its current status. */
data class TenderTarget(
    var code: String,
    var name: String,
    var attempts: Int,
    var routeCode: String,
    var stopCount: Int,
    var driverId: String,
    var version: Int,
    var capacity: Int,
    var country: String,
    var status: TenderTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderTargetStatus): Boolean {
        when (status) {
            TenderTargetStatus.NEW -> {
                when (next) {
                    TenderTargetStatus.RUNNING -> return true
                }
                return false
            }
            TenderTargetStatus.QUEUED -> {
                when (next) {
                    TenderTargetStatus.RUNNING -> return true
                }
                return false
            }
            TenderTargetStatus.RUNNING -> {
                when (next) {
                    TenderTargetStatus.DONE -> return true
                }
                return false
            }
            TenderTargetStatus.DONE -> {
                when (next) {
                    TenderTargetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderTargetStatus) {
        if (!canMoveTo(next)) {
            error("tender target $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"tender target"} $code"

    fun netLoad(): Double = round((capacity.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): TenderTarget {
        return TenderTarget(
            code = code,
            name = name,
            attempts = attempts,
            routeCode = routeCode,
            stopCount = stopCount,
            driverId = driverId,
            version = version,
            capacity = capacity,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tender target in status new. */
fun newTenderTarget(code: String, name: String): TenderTarget {
    return TenderTarget(
        code = code,
        name = name,
        attempts = 0,
        routeCode = "",
        stopCount = 0,
        driverId = "",
        version = 0,
        capacity = 0,
        country = "",
        status = TenderTargetStatus.NEW,
    )
}
