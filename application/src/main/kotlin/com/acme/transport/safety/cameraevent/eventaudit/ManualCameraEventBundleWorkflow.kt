package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves manual camera event bundles through their life cycle and keeps a journal of what it did. */
data class ManualCameraEventBundleService(
    var store: ManualCameraEventBundleStore,
    var validator: ManualCameraEventBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualCameraEventBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual camera event bundle $code not found")
        validator.requireValid(item)
        item.moveTo(ManualCameraEventBundleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual camera event bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualCameraEventBundleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual camera event bundle $code not found")
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

fun newManualCameraEventBundleService(store: ManualCameraEventBundleStore, clock: Clock, flags: Registry): ManualCameraEventBundleService {
    return ManualCameraEventBundleService(
        store = store,
        validator = defaultManualCameraEventBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualCameraEventBundleEngine(flags),
    )
}
