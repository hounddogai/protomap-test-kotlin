package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.clock.Clock

/** Moves weighted accessorial profiles through their life cycle and keeps a journal of what it did. */
data class WeightedAccessorialProfileCoordinator(
    var store: WeightedAccessorialProfileStore,
    var validator: WeightedAccessorialProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAccessorialProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted accessorial profile $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAccessorialProfileStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted accessorial profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAccessorialProfileStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted accessorial profile $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newWeightedAccessorialProfileCoordinator(store: WeightedAccessorialProfileStore, clock: Clock): WeightedAccessorialProfileCoordinator {
    return WeightedAccessorialProfileCoordinator(
        store = store,
        validator = defaultWeightedAccessorialProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAccessorialProfileEngine(),
    )
}
