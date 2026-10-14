package com.acme.transport.customs.tariff.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff manifest records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff manifest is in its life cycle. */
enum class TariffManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tariff manifest and its current status. */
data class TariffManifest(
    var code: String,
    var name: String,
    var endsAt: Int,
    var retries: Int,
    var dock: String,
    var category: String,
    var loadKg: Double,
    var score: Double,
    var trailerId: String,
    var sequence: Int,
    var shiftCode: String,
    var status: TariffManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffManifestStatus): Boolean {
        when (status) {
            TariffManifestStatus.NEW -> return next == TariffManifestStatus.FAILED
            TariffManifestStatus.QUEUED -> return next == TariffManifestStatus.FAILED
            TariffManifestStatus.RUNNING -> return next == TariffManifestStatus.DONE || next == TariffManifestStatus.FAILED
            TariffManifestStatus.DONE -> return next == TariffManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TariffManifestStatus) {
        if (!canMoveTo(next)) {
            error("tariff manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round((loadKg + retries.toDouble()) * 100) / 100

    fun snapshot(): TariffManifest {
        return TariffManifest(
            code = code,
            name = name,
            endsAt = endsAt,
            retries = retries,
            dock = dock,
            category = category,
            loadKg = loadKg,
            score = score,
            trailerId = trailerId,
            sequence = sequence,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tariff manifest in status new. */
fun newTariffManifest(code: String, name: String): TariffManifest {
    return TariffManifest(
        code = code,
        name = name,
        endsAt = 0,
        retries = 0,
        dock = "",
        category = "",
        loadKg = 0.0,
        score = 0.0,
        trailerId = "",
        sequence = 0,
        shiftCode = "",
        status = TariffManifestStatus.NEW,
    )
}
