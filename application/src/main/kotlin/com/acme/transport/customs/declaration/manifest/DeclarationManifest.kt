package com.acme.transport.customs.declaration.manifest

import com.acme.transport.kit.text.normalize

// The declaration manifest records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration manifest is in its life cycle. */
enum class DeclarationManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A declaration manifest. Its code is unique within a tenant. */
data class DeclarationManifest(
    var code: String,
    var title: String,
    var limit: Int,
    var tags: MutableList<String>,
    var externalRef: String,
    var name: String,
    var ownerId: String,
    var status: DeclarationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationManifestStatus): Boolean {
        when (status) {
            DeclarationManifestStatus.PROPOSED -> return next == DeclarationManifestStatus.APPROVED
            DeclarationManifestStatus.APPROVED -> {
                when (next) {
                    DeclarationManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            DeclarationManifestStatus.REJECTED -> return next == DeclarationManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DeclarationManifestStatus) {
        if (!canMoveTo(next)) {
            error("declaration manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"declaration manifest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): DeclarationManifest {
        return DeclarationManifest(
            code = code,
            title = title,
            limit = limit,
            tags = tags,
            externalRef = externalRef,
            name = name,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration manifest in status proposed. */
fun newDeclarationManifest(code: String, title: String): DeclarationManifest {
    return DeclarationManifest(
        code = code,
        title = title,
        limit = 0,
        tags = mutableListOf<String>(),
        externalRef = "",
        name = "",
        ownerId = "",
        status = DeclarationManifestStatus.PROPOSED,
    )
}
