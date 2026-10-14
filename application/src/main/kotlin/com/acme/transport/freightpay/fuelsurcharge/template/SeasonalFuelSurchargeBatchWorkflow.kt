package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves seasonal fuel surcharge batches through their life cycle and keeps a journal of what it did.
 */
data class SeasonalFuelSurchargeBatchService(
    var store: SeasonalFuelSurchargeBatchStore,
    var validator: SeasonalFuelSurchargeBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal fuel surcharge batch $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalFuelSurchargeBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal fuel surcharge batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalFuelSurchargeBatchStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newSeasonalFuelSurchargeBatchService(store: SeasonalFuelSurchargeBatchStore, clock: Clock): SeasonalFuelSurchargeBatchService {
    return SeasonalFuelSurchargeBatchService(
        store = store,
        validator = defaultSeasonalFuelSurchargeBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
