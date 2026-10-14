package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.clock.Clock

/** Moves manual qualification targets through their life cycle and keeps a journal of what it did. */
data class ManualQualificationTargetDesk(
    var store: ManualQualificationTargetStore,
    var validator: ManualQualificationTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualQualificationTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual qualification target $code not found")
        validator.requireValid(item)
        item.moveTo(ManualQualificationTargetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual qualification target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualQualificationTargetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual qualification target $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newManualQualificationTargetDesk(store: ManualQualificationTargetStore, clock: Clock): ManualQualificationTargetDesk {
    return ManualQualificationTargetDesk(
        store = store,
        validator = defaultManualQualificationTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualQualificationTargetEngine(),
    )
}
