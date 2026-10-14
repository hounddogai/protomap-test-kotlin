package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration exception records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration exception is in its life cycle. */
enum class DeclarationExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A declaration exception. Its code is unique within a tenant. */
data class DeclarationException(
    var code: String,
    var name: String,
    var currency: String,
    var revision: Int,
    var ratio: Double,
    var status: DeclarationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationExceptionStatus): Boolean {
        when (status) {
            DeclarationExceptionStatus.NEW -> return next == DeclarationExceptionStatus.FAILED
            DeclarationExceptionStatus.QUEUED -> {
                when (next) {
                    DeclarationExceptionStatus.DONE -> return true
                }
                return false
            }
            DeclarationExceptionStatus.RUNNING -> {
                when (next) {
                    DeclarationExceptionStatus.DONE -> return true
                }
                return false
            }
            DeclarationExceptionStatus.DONE -> return next == DeclarationExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DeclarationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("declaration exception $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"declaration exception"} $code"

    fun effectiveVolume(): Double = round((ratio - revision.toDouble()) * 100) / 100

    fun snapshot(): DeclarationException {
        return DeclarationException(
            code = code,
            name = name,
            currency = currency,
            revision = revision,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration exception in status new. */
fun newDeclarationException(code: String, name: String): DeclarationException {
    return DeclarationException(
        code = code,
        name = name,
        currency = "",
        revision = 0,
        ratio = 0.0,
        status = DeclarationExceptionStatus.NEW,
    )
}
