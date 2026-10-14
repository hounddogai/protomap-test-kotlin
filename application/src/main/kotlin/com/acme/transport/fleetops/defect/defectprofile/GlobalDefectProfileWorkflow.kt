package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global defect profiles through their life cycle and keeps a journal of what it did. */
data class GlobalDefectProfileManager(
    var store: GlobalDefectProfileStore,
    var validator: GlobalDefectProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalDefectProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global defect profile $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDefectProfileStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global defect profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDefectProfileStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global defect profile $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newGlobalDefectProfileManager(store: GlobalDefectProfileStore, clock: Clock, flags: Registry): GlobalDefectProfileManager {
    return GlobalDefectProfileManager(
        store = store,
        validator = defaultGlobalDefectProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalDefectProfileEngine(flags),
    )
}
