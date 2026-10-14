package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves qualification bundles through their life cycle and keeps a journal of what it did. */
data class QualificationBundleDesk(
    var store: QualificationBundleStore,
    var validator: QualificationBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("qualification bundle $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationBundleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationBundleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("qualification bundle $code not found")
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

fun newQualificationBundleDesk(store: QualificationBundleStore, clock: Clock, flags: Registry): QualificationBundleDesk {
    return QualificationBundleDesk(
        store = store,
        validator = defaultQualificationBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationBundleEngine(flags),
    )
}
