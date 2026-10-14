package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary inspection overrides through their life cycle and keeps a journal of what it did. */
data class PrimaryInspectionOverrideService(
    var store: PrimaryInspectionOverrideStore,
    var validator: PrimaryInspectionOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryInspectionOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary inspection override $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryInspectionOverrideStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary inspection override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryInspectionOverrideStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary inspection override $code not found")
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

fun newPrimaryInspectionOverrideService(store: PrimaryInspectionOverrideStore, clock: Clock, flags: Registry): PrimaryInspectionOverrideService {
    return PrimaryInspectionOverrideService(
        store = store,
        validator = defaultPrimaryInspectionOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryInspectionOverrideEngine(flags),
    )
}
