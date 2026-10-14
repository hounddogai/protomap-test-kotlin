package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.fleetops.clock.Clock

/** Moves defect batches through their life cycle and keeps a journal of what it did. */
data class DefectBatchManager(
    var store: DefectBatchStore,
    var validator: DefectBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefectBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("defect batch $code not found")
        validator.requireValid(item)
        item.moveTo(DefectBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("defect batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectBatchStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("defect batch $code not found")
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

fun newDefectBatchManager(store: DefectBatchStore, clock: Clock): DefectBatchManager {
    return DefectBatchManager(
        store = store,
        validator = defaultDefectBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefectBatchEngine(),
    )
}
