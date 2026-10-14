package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.freightpay.clock.Clock

/** Moves primary accessorial overrides through their life cycle and keeps a journal of what it did. */
data class PrimaryAccessorialOverrideService(
    var store: PrimaryAccessorialOverrideStore,
    var validator: PrimaryAccessorialOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryAccessorialOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary accessorial override $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAccessorialOverrideStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary accessorial override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAccessorialOverrideStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary accessorial override $code not found")
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

fun newPrimaryAccessorialOverrideService(store: PrimaryAccessorialOverrideStore, clock: Clock): PrimaryAccessorialOverrideService {
    return PrimaryAccessorialOverrideService(
        store = store,
        validator = defaultPrimaryAccessorialOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryAccessorialOverrideEngine(),
    )
}
