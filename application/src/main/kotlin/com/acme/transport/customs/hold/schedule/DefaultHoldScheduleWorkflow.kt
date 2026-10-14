package com.acme.transport.customs.hold.schedule

import com.acme.transport.customs.clock.Clock

/** Moves default hold schedules through their life cycle and keeps a journal of what it did. */
data class DefaultHoldScheduleCoordinator(
    var store: DefaultHoldScheduleStore,
    var validator: DefaultHoldScheduleValidatorExtra,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultHoldScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default hold schedule $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultHoldScheduleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default hold schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultHoldScheduleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default hold schedule $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newDefaultHoldScheduleCoordinator(store: DefaultHoldScheduleStore, clock: Clock): DefaultHoldScheduleCoordinator {
    return DefaultHoldScheduleCoordinator(
        store = store,
        validator = defaultDefaultHoldScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultHoldScheduleEngine(),
    )
}
