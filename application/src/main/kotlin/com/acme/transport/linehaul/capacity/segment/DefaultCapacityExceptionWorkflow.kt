package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.linehaul.clock.Clock

/** Moves default capacity exceptions through their life cycle and keeps a journal of what it did. */
data class DefaultCapacityExceptionCoordinator(
    var store: DefaultCapacityExceptionStore,
    var validator: DefaultCapacityExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultCapacityExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default capacity exception $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCapacityExceptionStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default capacity exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCapacityExceptionStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default capacity exception $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newDefaultCapacityExceptionCoordinator(store: DefaultCapacityExceptionStore, clock: Clock): DefaultCapacityExceptionCoordinator {
    return DefaultCapacityExceptionCoordinator(
        store = store,
        validator = defaultDefaultCapacityExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultCapacityExceptionEngine(),
    )
}
