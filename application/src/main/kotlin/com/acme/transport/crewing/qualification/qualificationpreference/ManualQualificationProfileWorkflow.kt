package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual qualification profiles through their life cycle and keeps a journal of what it did. */
data class ManualQualificationProfileService(
    var store: ManualQualificationProfileStore,
    var validator: ManualQualificationProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualQualificationProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual qualification profile $code not found")
        validator.requireValid(item)
        item.moveTo(ManualQualificationProfileStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual qualification profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualQualificationProfileStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual qualification profile $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newManualQualificationProfileService(store: ManualQualificationProfileStore, clock: Clock, flags: Registry): ManualQualificationProfileService {
    return ManualQualificationProfileService(
        store = store,
        validator = defaultManualQualificationProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualQualificationProfileEngine(flags),
    )
}
