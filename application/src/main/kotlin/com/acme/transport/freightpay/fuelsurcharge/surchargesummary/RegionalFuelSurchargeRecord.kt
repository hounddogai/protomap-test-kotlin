package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional fuel surcharge record is in its life cycle. */
enum class RegionalFuelSurchargeRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional fuel surcharge record as the fuel surcharge team keeps it. */
data class RegionalFuelSurchargeRecord(
    var code: String,
    var name: String,
    var carrier: String,
    var visible: Boolean,
    var active: Boolean,
    var floorValue: Double,
    var title: String,
    var capacity: Int,
    var externalRef: String,
    var priority: Int,
    var cost: Double,
    var status: RegionalFuelSurchargeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelSurchargeRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelSurchargeRecordStatus): Boolean {
        when (status) {
            RegionalFuelSurchargeRecordStatus.IDLE -> return next == RegionalFuelSurchargeRecordStatus.READY || next == RegionalFuelSurchargeRecordStatus.STOPPED
            RegionalFuelSurchargeRecordStatus.WARMING -> {
                when (next) {
                    RegionalFuelSurchargeRecordStatus.STOPPED -> return true
                }
                return false
            }
            RegionalFuelSurchargeRecordStatus.READY -> {
                when (next) {
                    RegionalFuelSurchargeRecordStatus.DRAINING -> return true
                }
                return false
            }
            RegionalFuelSurchargeRecordStatus.DRAINING -> {
                when (next) {
                    RegionalFuelSurchargeRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelSurchargeRecordStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel surcharge record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netLoad(): Double = round((floorValue + capacity.toDouble()) * 100) / 100

    fun snapshot(): RegionalFuelSurchargeRecord {
        return RegionalFuelSurchargeRecord(
            code = code,
            name = name,
            carrier = carrier,
            visible = visible,
            active = active,
            floorValue = floorValue,
            title = title,
            capacity = capacity,
            externalRef = externalRef,
            priority = priority,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional fuel surcharge record in status idle. */
fun newRegionalFuelSurchargeRecord(code: String, name: String): RegionalFuelSurchargeRecord {
    return RegionalFuelSurchargeRecord(
        code = code,
        name = name,
        carrier = "",
        visible = false,
        active = false,
        floorValue = 0.0,
        title = "",
        capacity = 0,
        externalRef = "",
        priority = 0,
        cost = 0.0,
        status = RegionalFuelSurchargeRecordStatus.IDLE,
    )
}
