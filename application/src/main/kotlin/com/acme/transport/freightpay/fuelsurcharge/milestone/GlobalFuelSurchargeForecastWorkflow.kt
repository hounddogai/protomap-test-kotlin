package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves global fuel surcharge forecasts through their life cycle and keeps a journal of what it did.
 */
data class GlobalFuelSurchargeForecastWorkflow(
    var store: GlobalFuelSurchargeForecastStore,
    var validator: GlobalFuelSurchargeForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global fuel surcharge forecast $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalFuelSurchargeForecastStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global fuel surcharge forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalFuelSurchargeForecastStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newGlobalFuelSurchargeForecastWorkflow(store: GlobalFuelSurchargeForecastStore, clock: Clock): GlobalFuelSurchargeForecastWorkflow {
    return GlobalFuelSurchargeForecastWorkflow(
        store = store,
        validator = defaultGlobalFuelSurchargeForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
