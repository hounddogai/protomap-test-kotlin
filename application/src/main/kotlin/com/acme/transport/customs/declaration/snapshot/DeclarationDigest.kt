package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration digest is in its life cycle. */
enum class DeclarationDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A declaration digest. Its code is unique within a tenant. */
data class DeclarationDigest(
    var code: String,
    var name: String,
    var archived: Boolean,
    var pinned: Boolean,
    var retries: Int,
    var routeCode: String,
    var updatedAt: Int,
    var weight: Double,
    var status: DeclarationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationDigestStatus): Boolean {
        when (status) {
            DeclarationDigestStatus.NEW -> {
                when (next) {
                    DeclarationDigestStatus.QUEUED, DeclarationDigestStatus.DONE -> return true
                }
                return false
            }
            DeclarationDigestStatus.QUEUED -> return next == DeclarationDigestStatus.DONE || next == DeclarationDigestStatus.FAILED
            DeclarationDigestStatus.RUNNING -> return next == DeclarationDigestStatus.DONE
            DeclarationDigestStatus.DONE -> {
                when (next) {
                    DeclarationDigestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationDigestStatus) {
        if (!canMoveTo(next)) {
            error("declaration digest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"declaration digest"} $code"

    fun weightedLoad(): Double = round(updatedAt.toDouble() * weight * 100) / 100

    fun snapshot(): DeclarationDigest {
        return DeclarationDigest(
            code = code,
            name = name,
            archived = archived,
            pinned = pinned,
            retries = retries,
            routeCode = routeCode,
            updatedAt = updatedAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration digest in status new. */
fun newDeclarationDigest(code: String, name: String): DeclarationDigest {
    return DeclarationDigest(
        code = code,
        name = name,
        archived = false,
        pinned = false,
        retries = 0,
        routeCode = "",
        updatedAt = 0,
        weight = 0.0,
        status = DeclarationDigestStatus.NEW,
    )
}
