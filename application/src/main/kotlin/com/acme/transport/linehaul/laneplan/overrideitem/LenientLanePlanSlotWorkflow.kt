package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient lane plan slots through their life cycle and keeps a journal of what it did. */
data class LenientLanePlanSlotService(
    var store: LenientLanePlanSlotStore,
    var validator: LenientLanePlanSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientLanePlanSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient lane plan slot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientLanePlanSlotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient lane plan slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientLanePlanSlotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient lane plan slot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientLanePlanSlotService(store: LenientLanePlanSlotStore, clock: Clock): LenientLanePlanSlotService {
    return LenientLanePlanSlotService(
        store = store,
        validator = defaultLenientLanePlanSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientLanePlanSlotEngine(),
    )
}
