package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.normalize

/** Where a global declaration audit is in its life cycle. */
enum class GlobalDeclarationAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global declaration audit and its current status. */
data class GlobalDeclarationAudit(
    var code: String,
    var name: String,
    var checksum: String,
    var phone: String,
    var source: String,
    var amount: Double,
    var status: GlobalDeclarationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDeclarationAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDeclarationAuditStatus): Boolean {
        when (status) {
            GlobalDeclarationAuditStatus.NEW -> return next == GlobalDeclarationAuditStatus.QUEUED || next == GlobalDeclarationAuditStatus.RUNNING
            GlobalDeclarationAuditStatus.QUEUED -> return next == GlobalDeclarationAuditStatus.RUNNING || next == GlobalDeclarationAuditStatus.DONE
            GlobalDeclarationAuditStatus.RUNNING -> {
                when (next) {
                    GlobalDeclarationAuditStatus.DONE -> return true
                }
                return false
            }
            GlobalDeclarationAuditStatus.DONE -> return next == GlobalDeclarationAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalDeclarationAuditStatus) {
        if (!canMoveTo(next)) {
            error("global declaration audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global declaration audit"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): GlobalDeclarationAudit {
        return GlobalDeclarationAudit(
            code = code,
            name = name,
            checksum = checksum,
            phone = phone,
            source = source,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global declaration audit in status new. */
fun newGlobalDeclarationAudit(code: String, name: String): GlobalDeclarationAudit {
    return GlobalDeclarationAudit(
        code = code,
        name = name,
        checksum = "",
        phone = "",
        source = "",
        amount = 0.0,
        status = GlobalDeclarationAuditStatus.NEW,
    )
}
