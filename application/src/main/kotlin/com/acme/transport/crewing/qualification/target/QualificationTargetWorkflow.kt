package com.acme.transport.crewing.qualification.target

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves qualification targets through their life cycle and keeps a journal of what it did. */
data class QualificationTargetCoordinator(
    var store: QualificationTargetStore,
    var validator: QualificationTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("qualification target $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationTargetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationTargetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("qualification target $code not found")
        val result = engine.evaluate(item)
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

fun newQualificationTargetCoordinator(store: QualificationTargetStore, clock: Clock, flags: Registry): QualificationTargetCoordinator {
    return QualificationTargetCoordinator(
        store = store,
        validator = defaultQualificationTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationTargetEngine(flags),
    )
}
