package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves violation bundles through their life cycle and keeps a journal of what it did. */
data class ViolationBundleDesk(
    var store: ViolationBundleStore,
    var validator: ViolationBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("violation bundle $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationBundleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("violation bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationBundleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("violation bundle $code not found")
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

fun newViolationBundleDesk(store: ViolationBundleStore, clock: Clock, flags: Registry): ViolationBundleDesk {
    return ViolationBundleDesk(
        store = store,
        validator = defaultViolationBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationBundleEngine(flags),
    )
}
