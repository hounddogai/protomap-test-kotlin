package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.linehaul.clock.Clock

/** Moves rolling tender groups through their life cycle and keeps a journal of what it did. */
data class RollingTenderGroupService(
    var store: RollingTenderGroupStore,
    var validator: RollingTenderGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingTenderGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling tender group $code not found")
        validator.requireValid(item)
        item.moveTo(RollingTenderGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling tender group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingTenderGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling tender group $code not found")
        val result = engine.price(item)
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

fun newRollingTenderGroupService(store: RollingTenderGroupStore, clock: Clock): RollingTenderGroupService {
    return RollingTenderGroupService(
        store = store,
        validator = defaultRollingTenderGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingTenderGroupEngine(),
    )
}
