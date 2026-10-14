package com.acme.transport.customs.tariff.entry

import com.acme.transport.kit.text.normalize

/** Where a tariff snapshot is in its life cycle. */
enum class TariffSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tariff snapshot and its current status. */
data class TariffSnapshot(
    var code: String,
    var name: String,
    var routeCode: String,
    var trailerId: String,
    var reviewer: String,
    var score: Double,
    var status: TariffSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffSnapshotStatus): Boolean {
        when (status) {
            TariffSnapshotStatus.NEW -> return next == TariffSnapshotStatus.FAILED
            TariffSnapshotStatus.QUEUED -> {
                when (next) {
                    TariffSnapshotStatus.RUNNING, TariffSnapshotStatus.FAILED -> return true
                }
                return false
            }
            TariffSnapshotStatus.RUNNING -> {
                when (next) {
                    TariffSnapshotStatus.DONE, TariffSnapshotStatus.FAILED -> return true
                }
                return false
            }
            TariffSnapshotStatus.DONE -> return next == TariffSnapshotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TariffSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("tariff snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = score

    fun snapshot(): TariffSnapshot {
        return TariffSnapshot(
            code = code,
            name = name,
            routeCode = routeCode,
            trailerId = trailerId,
            reviewer = reviewer,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tariff snapshot in status new. */
fun newTariffSnapshot(code: String, name: String): TariffSnapshot {
    return TariffSnapshot(
        code = code,
        name = name,
        routeCode = "",
        trailerId = "",
        reviewer = "",
        score = 0.0,
        status = TariffSnapshotStatus.NEW,
    )
}
