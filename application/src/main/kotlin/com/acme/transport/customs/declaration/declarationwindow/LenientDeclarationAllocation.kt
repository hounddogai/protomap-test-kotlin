package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient declaration allocation is in its life cycle. */
enum class LenientDeclarationAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient declaration allocation. Its code is unique within a tenant. */
data class LenientDeclarationAllocation(
    var code: String,
    var name: String,
    var maximum: Double,
    var category: String,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var status: LenientDeclarationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDeclarationAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDeclarationAllocationStatus): Boolean {
        when (status) {
            LenientDeclarationAllocationStatus.DRAFT -> {
                when (next) {
                    LenientDeclarationAllocationStatus.PENDING, LenientDeclarationAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientDeclarationAllocationStatus.PENDING -> return next == LenientDeclarationAllocationStatus.ACTIVE
            LenientDeclarationAllocationStatus.ACTIVE -> return next == LenientDeclarationAllocationStatus.SUSPENDED || next == LenientDeclarationAllocationStatus.CLOSED
            LenientDeclarationAllocationStatus.SUSPENDED -> {
                when (next) {
                    LenientDeclarationAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDeclarationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("lenient declaration allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round(startsAt.toDouble() * maximum * 100) / 100

    fun snapshot(): LenientDeclarationAllocation {
        return LenientDeclarationAllocation(
            code = code,
            name = name,
            maximum = maximum,
            category = category,
            attributes = attributes,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient declaration allocation in status draft. */
fun newLenientDeclarationAllocation(code: String, name: String): LenientDeclarationAllocation {
    return LenientDeclarationAllocation(
        code = code,
        name = name,
        maximum = 0.0,
        category = "",
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        status = LenientDeclarationAllocationStatus.DRAFT,
    )
}
