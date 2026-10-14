package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves defect profiles through their life cycle and keeps a journal of what it did. */
data class DefectProfileWorkflow(
    var store: DefectProfileStore,
    var validator: DefectProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefectProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("defect profile $code not found")
        validator.requireValid(item)
        item.moveTo(DefectProfileStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("defect profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectProfileStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("defect profile $code not found")
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

fun newDefectProfileWorkflow(store: DefectProfileStore, clock: Clock, flags: Registry): DefectProfileWorkflow {
    return DefectProfileWorkflow(
        store = store,
        validator = defaultDefectProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefectProfileEngine(flags),
    )
}
