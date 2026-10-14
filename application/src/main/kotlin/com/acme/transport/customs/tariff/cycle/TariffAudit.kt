package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff audit is in its life cycle. */
enum class TariffAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tariff audit as the tariff team keeps it. */
data class TariffAudit(
    var code: String,
    var name: String,
    var visible: Boolean,
    var startsAt: Int,
    var active: Boolean,
    var ownerId: String,
    var source: String,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var discount: Double,
    var status: TariffAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffAuditStatus): Boolean {
        when (status) {
            TariffAuditStatus.NEW -> return next == TariffAuditStatus.FAILED
            TariffAuditStatus.QUEUED -> return next == TariffAuditStatus.RUNNING || next == TariffAuditStatus.FAILED
            TariffAuditStatus.RUNNING -> {
                when (next) {
                    TariffAuditStatus.DONE, TariffAuditStatus.FAILED -> return true
                }
                return false
            }
            TariffAuditStatus.DONE -> return next == TariffAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TariffAuditStatus) {
        if (!canMoveTo(next)) {
            error("tariff audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(startsAt.toDouble() * discount * 100) / 100

    fun snapshot(): TariffAudit {
        return TariffAudit(
            code = code,
            name = name,
            visible = visible,
            startsAt = startsAt,
            active = active,
            ownerId = ownerId,
            source = source,
            attributes = attributes,
            threshold = threshold,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tariff audit in status new. */
fun newTariffAudit(code: String, name: String): TariffAudit {
    return TariffAudit(
        code = code,
        name = name,
        visible = false,
        startsAt = 0,
        active = false,
        ownerId = "",
        source = "",
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        discount = 0.0,
        status = TariffAuditStatus.NEW,
    )
}
