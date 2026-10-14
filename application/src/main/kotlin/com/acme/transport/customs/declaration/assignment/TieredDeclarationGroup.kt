package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.text.normalize

/** Where a tiered declaration group is in its life cycle. */
enum class TieredDeclarationGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a tiered declaration group and its current status. */
data class TieredDeclarationGroup(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var contactEmail: String,
    var title: String,
    var rate: Double,
    var status: TieredDeclarationGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDeclarationGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDeclarationGroupStatus): Boolean {
        when (status) {
            TieredDeclarationGroupStatus.DRAFT -> {
                when (next) {
                    TieredDeclarationGroupStatus.PENDING -> return true
                }
                return false
            }
            TieredDeclarationGroupStatus.PENDING -> return next == TieredDeclarationGroupStatus.SUSPENDED || next == TieredDeclarationGroupStatus.CLOSED
            TieredDeclarationGroupStatus.ACTIVE -> return next == TieredDeclarationGroupStatus.SUSPENDED
            TieredDeclarationGroupStatus.SUSPENDED -> return next == TieredDeclarationGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TieredDeclarationGroupStatus) {
        if (!canMoveTo(next)) {
            error("tiered declaration group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = rate

    fun snapshot(): TieredDeclarationGroup {
        return TieredDeclarationGroup(
            code = code,
            name = name,
            tags = tags,
            contactEmail = contactEmail,
            title = title,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered declaration group in status draft. */
fun newTieredDeclarationGroup(code: String, name: String): TieredDeclarationGroup {
    return TieredDeclarationGroup(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        contactEmail = "",
        title = "",
        rate = 0.0,
        status = TieredDeclarationGroupStatus.DRAFT,
    )
}
