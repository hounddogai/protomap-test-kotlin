package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dock door manifests through their life cycle and keeps a journal of what it did. */
data class DockDoorManifestService(
    var store: DockDoorManifestStore,
    var validator: DockDoorManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dock door manifest $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorManifestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorManifestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("dock door manifest $code not found")
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

fun newDockDoorManifestService(store: DockDoorManifestStore, clock: Clock, flags: Registry): DockDoorManifestService {
    return DockDoorManifestService(
        store = store,
        validator = defaultDockDoorManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorManifestEngine(flags),
    )
}
