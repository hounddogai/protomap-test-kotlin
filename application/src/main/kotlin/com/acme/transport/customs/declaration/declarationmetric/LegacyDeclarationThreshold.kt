package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy declaration threshold is in its life cycle. */
enum class LegacyDeclarationThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy declaration threshold. Its code is unique within a tenant. */
data class LegacyDeclarationThreshold(
    var code: String,
    var title: String,
    var ceiling: Double,
    var threshold: Double,
    var unit: String,
    var approved: Boolean,
    var limit: Int,
    var country: String,
    var phone: String,
    var name: String,
    var sequence: Int,
    var status: LegacyDeclarationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDeclarationThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDeclarationThresholdStatus): Boolean {
        when (status) {
            LegacyDeclarationThresholdStatus.DRAFT -> {
                when (next) {
                    LegacyDeclarationThresholdStatus.PENDING, LegacyDeclarationThresholdStatus.ACTIVE -> return true
                }
                return false
            }
            LegacyDeclarationThresholdStatus.PENDING -> {
                when (next) {
                    LegacyDeclarationThresholdStatus.CLOSED -> return true
                }
                return false
            }
            LegacyDeclarationThresholdStatus.ACTIVE -> return next == LegacyDeclarationThresholdStatus.CLOSED
            LegacyDeclarationThresholdStatus.SUSPENDED -> {
                when (next) {
                    LegacyDeclarationThresholdStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDeclarationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("legacy declaration threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedVolume(): Double = round((sequence.toDouble() - ceiling) * 100) / 100

    fun snapshot(): LegacyDeclarationThreshold {
        return LegacyDeclarationThreshold(
            code = code,
            title = title,
            ceiling = ceiling,
            threshold = threshold,
            unit = unit,
            approved = approved,
            limit = limit,
            country = country,
            phone = phone,
            name = name,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy declaration threshold in status draft. */
fun newLegacyDeclarationThreshold(code: String, title: String): LegacyDeclarationThreshold {
    return LegacyDeclarationThreshold(
        code = code,
        title = title,
        ceiling = 0.0,
        threshold = 0.0,
        unit = "",
        approved = false,
        limit = 0,
        country = "",
        phone = "",
        name = "",
        sequence = 0,
        status = LegacyDeclarationThresholdStatus.DRAFT,
    )
}
