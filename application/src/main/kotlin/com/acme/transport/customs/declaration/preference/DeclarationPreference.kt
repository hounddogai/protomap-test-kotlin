package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration preference records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration preference is in its life cycle. */
enum class DeclarationPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A declaration preference as the declaration team keeps it. */
data class DeclarationPreference(
    var code: String,
    var title: String,
    var threshold: Double,
    var score: Double,
    var minimum: Double,
    var currency: String,
    var ownerId: String,
    var revision: Int,
    var shiftCode: String,
    var category: String,
    var status: DeclarationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationPreferenceStatus): Boolean {
        when (status) {
            DeclarationPreferenceStatus.OPEN -> return next == DeclarationPreferenceStatus.REOPENED
            DeclarationPreferenceStatus.IN_REVIEW -> {
                when (next) {
                    DeclarationPreferenceStatus.REOPENED, DeclarationPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            DeclarationPreferenceStatus.RESOLVED -> {
                when (next) {
                    DeclarationPreferenceStatus.REOPENED, DeclarationPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            DeclarationPreferenceStatus.REOPENED -> {
                when (next) {
                    DeclarationPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("declaration preference $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"declaration preference"} $code"

    fun netLoad(): Double = round(revision.toDouble() * minimum * 100) / 100

    fun snapshot(): DeclarationPreference {
        return DeclarationPreference(
            code = code,
            title = title,
            threshold = threshold,
            score = score,
            minimum = minimum,
            currency = currency,
            ownerId = ownerId,
            revision = revision,
            shiftCode = shiftCode,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a declaration preference in status open. */
fun newDeclarationPreference(code: String, title: String): DeclarationPreference {
    return DeclarationPreference(
        code = code,
        title = title,
        threshold = 0.0,
        score = 0.0,
        minimum = 0.0,
        currency = "",
        ownerId = "",
        revision = 0,
        shiftCode = "",
        category = "",
        status = DeclarationPreferenceStatus.OPEN,
    )
}
