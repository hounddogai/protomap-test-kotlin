package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration profile records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration profile is in its life cycle. */
enum class DeclarationProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One declaration profile, with what the planners need to act on it. */
data class DeclarationProfile(
    var code: String,
    var title: String,
    var loadKg: Double,
    var durationMinutes: Int,
    var endsAt: Int,
    var name: String,
    var status: DeclarationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationProfileStatus): Boolean {
        when (status) {
            DeclarationProfileStatus.OPEN -> {
                when (next) {
                    DeclarationProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            DeclarationProfileStatus.IN_REVIEW -> {
                when (next) {
                    DeclarationProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            DeclarationProfileStatus.RESOLVED -> {
                when (next) {
                    DeclarationProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            DeclarationProfileStatus.REOPENED -> return next == DeclarationProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DeclarationProfileStatus) {
        if (!canMoveTo(next)) {
            error("declaration profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((endsAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): DeclarationProfile {
        return DeclarationProfile(
            code = code,
            title = title,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration profile in status open. */
fun newDeclarationProfile(code: String, title: String): DeclarationProfile {
    return DeclarationProfile(
        code = code,
        title = title,
        loadKg = 0.0,
        durationMinutes = 0,
        endsAt = 0,
        name = "",
        status = DeclarationProfileStatus.OPEN,
    )
}
