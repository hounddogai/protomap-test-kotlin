package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered rest break manifests through their life cycle and keeps a journal of what it did. */
data class TieredRestBreakManifestManager(
    var store: TieredRestBreakManifestStore,
    var validator: TieredRestBreakManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredRestBreakManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered rest break manifest $code not found")
        validator.requireValid(item)
        item.moveTo(TieredRestBreakManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered rest break manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredRestBreakManifestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered rest break manifest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newTieredRestBreakManifestManager(store: TieredRestBreakManifestStore, clock: Clock, flags: Registry): TieredRestBreakManifestManager {
    return TieredRestBreakManifestManager(
        store = store,
        validator = defaultTieredRestBreakManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredRestBreakManifestEngine(flags),
    )
}
