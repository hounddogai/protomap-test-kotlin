package com.acme.transport.customs.inspection.schedule

import com.acme.transport.customs.clock.Clock

/** Moves strict inspection profiles through their life cycle and keeps a journal of what it did. */
data class StrictInspectionProfileService(
    var store: StrictInspectionProfileStore,
    var validator: StrictInspectionProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictInspectionProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict inspection profile $code not found")
        validator.requireValid(item)
        item.moveTo(StrictInspectionProfileStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict inspection profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictInspectionProfileStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict inspection profile $code not found")
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

fun newStrictInspectionProfileService(store: StrictInspectionProfileStore, clock: Clock): StrictInspectionProfileService {
    return StrictInspectionProfileService(
        store = store,
        validator = defaultStrictInspectionProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictInspectionProfileEngine(),
    )
}
