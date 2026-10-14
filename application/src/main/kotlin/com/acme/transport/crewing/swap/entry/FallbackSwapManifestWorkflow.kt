package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.clock.Clock

/** Moves fallback swap manifests through their life cycle and keeps a journal of what it did. */
data class FallbackSwapManifestService(
    var store: FallbackSwapManifestStore,
    var validator: FallbackSwapManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackSwapManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback swap manifest $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackSwapManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback swap manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackSwapManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback swap manifest $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newFallbackSwapManifestService(store: FallbackSwapManifestStore, clock: Clock): FallbackSwapManifestService {
    return FallbackSwapManifestService(
        store = store,
        validator = defaultFallbackSwapManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackSwapManifestEngine(),
    )
}
