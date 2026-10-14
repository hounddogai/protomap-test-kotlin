package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict declaration baseline is in its life cycle. */
enum class StrictDeclarationBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict declaration baseline. Its code is unique within a tenant. */
data class StrictDeclarationBaseline(
    var code: String,
    var name: String,
    var checksum: String,
    var discount: Double,
    var attributes: MutableMap<String, String>,
    var retries: Int,
    var notes: String,
    var updatedAt: Int,
    var visible: Boolean,
    var status: StrictDeclarationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDeclarationBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDeclarationBaselineStatus): Boolean {
        when (status) {
            StrictDeclarationBaselineStatus.OPEN -> {
                when (next) {
                    StrictDeclarationBaselineStatus.IN_REVIEW -> return true
                }
                return false
            }
            StrictDeclarationBaselineStatus.IN_REVIEW -> {
                when (next) {
                    StrictDeclarationBaselineStatus.RESOLVED -> return true
                }
                return false
            }
            StrictDeclarationBaselineStatus.RESOLVED -> {
                when (next) {
                    StrictDeclarationBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDeclarationBaselineStatus.REOPENED -> return next == StrictDeclarationBaselineStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictDeclarationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("strict declaration baseline $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round(retries.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): StrictDeclarationBaseline {
        return StrictDeclarationBaseline(
            code = code,
            name = name,
            checksum = checksum,
            discount = discount,
            attributes = attributes,
            retries = retries,
            notes = notes,
            updatedAt = updatedAt,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict declaration baseline in status open. */
fun newStrictDeclarationBaseline(code: String, name: String): StrictDeclarationBaseline {
    return StrictDeclarationBaseline(
        code = code,
        name = name,
        checksum = "",
        discount = 0.0,
        attributes = mutableMapOf<String, String>(),
        retries = 0,
        notes = "",
        updatedAt = 0,
        visible = false,
        status = StrictDeclarationBaselineStatus.OPEN,
    )
}
