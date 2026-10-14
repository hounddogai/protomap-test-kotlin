package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge batches through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeBatchService(
    var store: FuelSurchargeBatchStore,
    var validator: FuelSurchargeBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel surcharge batch $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeBatchStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeBatchStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newFuelSurchargeBatchService(store: FuelSurchargeBatchStore, clock: Clock): FuelSurchargeBatchService {
    return FuelSurchargeBatchService(
        store = store,
        validator = defaultFuelSurchargeBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
