package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.crewing.clock.Clock

/** Moves qualification overrides through their life cycle and keeps a journal of what it did. */
data class QualificationOverrideWorkflow(
    var store: QualificationOverrideStore,
    var validator: QualificationOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("qualification override $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationOverrideStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationOverrideStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("qualification override $code not found")
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

fun newQualificationOverrideWorkflow(store: QualificationOverrideStore, clock: Clock): QualificationOverrideWorkflow {
    return QualificationOverrideWorkflow(
        store = store,
        validator = defaultQualificationOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationOverrideEngine(),
    )
}
