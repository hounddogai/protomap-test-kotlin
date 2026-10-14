package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff bundle is in its life cycle. */
enum class TariffBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tariff bundle and its current status. */
data class TariffBundle(
    var code: String,
    var title: String,
    var createdAt: Int,
    var weight: Double,
    var category: String,
    var notes: String,
    var routeCode: String,
    var priority: Int,
    var ownerId: String,
    var attempts: Int,
    var sequence: Int,
    var status: TariffBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffBundleStatus): Boolean {
        when (status) {
            TariffBundleStatus.REQUESTED -> return next == TariffBundleStatus.REVOKED || next == TariffBundleStatus.EXPIRED
            TariffBundleStatus.GRANTED -> {
                when (next) {
                    TariffBundleStatus.REVOKED, TariffBundleStatus.EXPIRED -> return true
                }
                return false
            }
            TariffBundleStatus.REVOKED -> return next == TariffBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TariffBundleStatus) {
        if (!canMoveTo(next)) {
            error("tariff bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(createdAt.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): TariffBundle {
        return TariffBundle(
            code = code,
            title = title,
            createdAt = createdAt,
            weight = weight,
            category = category,
            notes = notes,
            routeCode = routeCode,
            priority = priority,
            ownerId = ownerId,
            attempts = attempts,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tariff bundle in status requested. */
fun newTariffBundle(code: String, title: String): TariffBundle {
    return TariffBundle(
        code = code,
        title = title,
        createdAt = 0,
        weight = 0.0,
        category = "",
        notes = "",
        routeCode = "",
        priority = 0,
        ownerId = "",
        attempts = 0,
        sequence = 0,
        status = TariffBundleStatus.REQUESTED,
    )
}
