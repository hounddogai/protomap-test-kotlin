package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves locker manifests through their life cycle and keeps a journal of what it did. */
data class LockerManifestWorkflow(
    var store: LockerManifestStore,
    var validator: LockerManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("locker manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LockerManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("locker manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("locker manifest $code not found")
        val result = engine.evaluate(item)
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

fun newLockerManifestWorkflow(store: LockerManifestStore, clock: Clock, flags: Registry): LockerManifestWorkflow {
    return LockerManifestWorkflow(
        store = store,
        validator = defaultLockerManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerManifestEngine(flags),
    )
}
