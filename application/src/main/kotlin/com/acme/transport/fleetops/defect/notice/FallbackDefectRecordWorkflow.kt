package com.acme.transport.fleetops.defect.notice

import com.acme.transport.fleetops.clock.Clock

/** Moves fallback defect records through their life cycle and keeps a journal of what it did. */
data class FallbackDefectRecordManager(
    var store: FallbackDefectRecordStore,
    var validator: FallbackDefectRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDefectRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback defect record $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDefectRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback defect record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDefectRecordStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback defect record $code not found")
        val result = engine.run(item)
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

fun newFallbackDefectRecordManager(store: FallbackDefectRecordStore, clock: Clock): FallbackDefectRecordManager {
    return FallbackDefectRecordManager(
        store = store,
        validator = defaultFallbackDefectRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDefectRecordEngine(),
    )
}
