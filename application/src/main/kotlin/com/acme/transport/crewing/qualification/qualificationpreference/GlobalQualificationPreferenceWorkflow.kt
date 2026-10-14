package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves global qualification preferences through their life cycle and keeps a journal of what it did.
 */
data class GlobalQualificationPreferenceDesk(
    var store: GlobalQualificationPreferenceStore,
    var validator: GlobalQualificationPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalQualificationPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global qualification preference $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalQualificationPreferenceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global qualification preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalQualificationPreferenceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global qualification preference $code not found")
        val result = engine.price(item)
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

fun newGlobalQualificationPreferenceDesk(store: GlobalQualificationPreferenceStore, clock: Clock, flags: Registry): GlobalQualificationPreferenceDesk {
    return GlobalQualificationPreferenceDesk(
        store = store,
        validator = defaultGlobalQualificationPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalQualificationPreferenceEngine(flags),
    )
}
