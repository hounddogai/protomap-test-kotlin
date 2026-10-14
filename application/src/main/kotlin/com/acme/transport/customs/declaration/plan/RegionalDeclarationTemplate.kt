package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional declaration template is in its life cycle. */
enum class RegionalDeclarationTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional declaration template. Its code is unique within a tenant. */
data class RegionalDeclarationTemplate(
    var code: String,
    var label: String,
    var capacity: Int,
    var revision: Int,
    var contactEmail: String,
    var amount: Double,
    var quantity: Int,
    var stopCount: Int,
    var archived: Boolean,
    var status: RegionalDeclarationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDeclarationTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDeclarationTemplateStatus): Boolean {
        when (status) {
            RegionalDeclarationTemplateStatus.DRAFT -> {
                when (next) {
                    RegionalDeclarationTemplateStatus.SUSPENDED, RegionalDeclarationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            RegionalDeclarationTemplateStatus.PENDING -> {
                when (next) {
                    RegionalDeclarationTemplateStatus.SUSPENDED, RegionalDeclarationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            RegionalDeclarationTemplateStatus.ACTIVE -> {
                when (next) {
                    RegionalDeclarationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            RegionalDeclarationTemplateStatus.SUSPENDED -> {
                when (next) {
                    RegionalDeclarationTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDeclarationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("regional declaration template $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"regional declaration template"} $code"

    fun adjustedValue(): Double = round((capacity.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): RegionalDeclarationTemplate {
        return RegionalDeclarationTemplate(
            code = code,
            label = label,
            capacity = capacity,
            revision = revision,
            contactEmail = contactEmail,
            amount = amount,
            quantity = quantity,
            stopCount = stopCount,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional declaration template in status draft. */
fun newRegionalDeclarationTemplate(code: String, label: String): RegionalDeclarationTemplate {
    return RegionalDeclarationTemplate(
        code = code,
        label = label,
        capacity = 0,
        revision = 0,
        contactEmail = "",
        amount = 0.0,
        quantity = 0,
        stopCount = 0,
        archived = false,
        status = RegionalDeclarationTemplateStatus.DRAFT,
    )
}
