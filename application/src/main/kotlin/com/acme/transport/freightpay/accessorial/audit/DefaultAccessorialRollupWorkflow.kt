package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default accessorial rollups through their life cycle and keeps a journal of what it did. */
data class DefaultAccessorialRollupDesk(
    var store: DefaultAccessorialRollupStore,
    var validator: DefaultAccessorialRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultAccessorialRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default accessorial rollup $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAccessorialRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default accessorial rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAccessorialRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default accessorial rollup $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefaultAccessorialRollupDesk(store: DefaultAccessorialRollupStore, clock: Clock, flags: Registry): DefaultAccessorialRollupDesk {
    return DefaultAccessorialRollupDesk(
        store = store,
        validator = defaultDefaultAccessorialRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultAccessorialRollupEngine(flags),
    )
}
