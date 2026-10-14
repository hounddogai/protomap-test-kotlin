package com.acme.transport.customs.tariff.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff entry records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff entry is in its life cycle. */
enum class TariffEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tariff entry. Its code is unique within a tenant. */
data class TariffEntry(
    var code: String,
    var label: String,
    var odometer: Int,
    var createdAt: Int,
    var title: String,
    var floorValue: Double,
    var locale: String,
    var status: TariffEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffEntryStatus): Boolean {
        when (status) {
            TariffEntryStatus.NEW -> {
                when (next) {
                    TariffEntryStatus.DONE -> return true
                }
                return false
            }
            TariffEntryStatus.QUEUED -> {
                when (next) {
                    TariffEntryStatus.FAILED -> return true
                }
                return false
            }
            TariffEntryStatus.RUNNING -> return next == TariffEntryStatus.DONE || next == TariffEntryStatus.FAILED
            TariffEntryStatus.DONE -> {
                when (next) {
                    TariffEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffEntryStatus) {
        if (!canMoveTo(next)) {
            error("tariff entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((odometer.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): TariffEntry {
        return TariffEntry(
            code = code,
            label = label,
            odometer = odometer,
            createdAt = createdAt,
            title = title,
            floorValue = floorValue,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tariff entry in status new. */
fun newTariffEntry(code: String, label: String): TariffEntry {
    return TariffEntry(
        code = code,
        label = label,
        odometer = 0,
        createdAt = 0,
        title = "",
        floorValue = 0.0,
        locale = "",
        status = TariffEntryStatus.NEW,
    )
}
