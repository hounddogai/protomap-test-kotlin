package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration template is in its life cycle. */
enum class DeclarationTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A declaration template. Its code is unique within a tenant. */
data class DeclarationTemplate(
    var code: String,
    var name: String,
    var active: Boolean,
    var batchSize: Int,
    var source: String,
    var rate: Double,
    var quantity: Int,
    var revision: Int,
    var minimum: Double,
    var windowDays: Int,
    var floorValue: Double,
    var status: DeclarationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationTemplateStatus): Boolean {
        when (status) {
            DeclarationTemplateStatus.IDLE -> {
                when (next) {
                    DeclarationTemplateStatus.WARMING, DeclarationTemplateStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationTemplateStatus.WARMING -> return next == DeclarationTemplateStatus.READY || next == DeclarationTemplateStatus.DRAINING
            DeclarationTemplateStatus.READY -> {
                when (next) {
                    DeclarationTemplateStatus.DRAINING, DeclarationTemplateStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationTemplateStatus.DRAINING -> {
                when (next) {
                    DeclarationTemplateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("declaration template $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round(windowDays.toDouble() * floorValue * 100) / 100

    fun snapshot(): DeclarationTemplate {
        return DeclarationTemplate(
            code = code,
            name = name,
            active = active,
            batchSize = batchSize,
            source = source,
            rate = rate,
            quantity = quantity,
            revision = revision,
            minimum = minimum,
            windowDays = windowDays,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a declaration template in status idle. */
fun newDeclarationTemplate(code: String, name: String): DeclarationTemplate {
    return DeclarationTemplate(
        code = code,
        name = name,
        active = false,
        batchSize = 0,
        source = "",
        rate = 0.0,
        quantity = 0,
        revision = 0,
        minimum = 0.0,
        windowDays = 0,
        floorValue = 0.0,
        status = DeclarationTemplateStatus.IDLE,
    )
}
