package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves tiered lane plan overrides through their life cycle and keeps a journal of what it did. */
data class TieredLanePlanOverrideManager(
    var store: TieredLanePlanOverrideStore,
    var validator: TieredLanePlanOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredLanePlanOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered lane plan override $code not found")
        validator.requireValid(item)
        item.moveTo(TieredLanePlanOverrideStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered lane plan override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredLanePlanOverrideStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered lane plan override $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newTieredLanePlanOverrideManager(store: TieredLanePlanOverrideStore, clock: Clock, flags: Registry): TieredLanePlanOverrideManager {
    return TieredLanePlanOverrideManager(
        store = store,
        validator = defaultTieredLanePlanOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredLanePlanOverrideEngine(flags),
    )
}
