package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit tier is in its life cycle. */
enum class AuditTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a audit tier and its current status. */
data class AuditTier(
    var code: String,
    var label: String,
    var pallets: Int,
    var dock: String,
    var createdAt: Int,
    var attributes: MutableMap<String, String>,
    var minimum: Double,
    var loadKg: Double,
    var phone: String,
    var lane: String,
    var quantity: Int,
    var status: AuditTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditTierStatus): Boolean {
        when (status) {
            AuditTierStatus.NEW -> {
                when (next) {
                    AuditTierStatus.DONE -> return true
                }
                return false
            }
            AuditTierStatus.QUEUED -> {
                when (next) {
                    AuditTierStatus.RUNNING, AuditTierStatus.DONE -> return true
                }
                return false
            }
            AuditTierStatus.RUNNING -> {
                when (next) {
                    AuditTierStatus.DONE, AuditTierStatus.FAILED -> return true
                }
                return false
            }
            AuditTierStatus.DONE -> {
                when (next) {
                    AuditTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditTierStatus) {
        if (!canMoveTo(next)) {
            error("audit tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"audit tier"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((quantity.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): AuditTier {
        return AuditTier(
            code = code,
            label = label,
            pallets = pallets,
            dock = dock,
            createdAt = createdAt,
            attributes = attributes,
            minimum = minimum,
            loadKg = loadKg,
            phone = phone,
            lane = lane,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a audit tier in status new. */
fun newAuditTier(code: String, label: String): AuditTier {
    return AuditTier(
        code = code,
        label = label,
        pallets = 0,
        dock = "",
        createdAt = 0,
        attributes = mutableMapOf<String, String>(),
        minimum = 0.0,
        loadKg = 0.0,
        phone = "",
        lane = "",
        quantity = 0,
        status = AuditTierStatus.NEW,
    )
}
