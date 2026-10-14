package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves qualification records through their life cycle and keeps a journal of what it did. */
data class QualificationRecordManager(
    var store: QualificationRecordStore,
    var validator: QualificationRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("qualification record $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationRecordStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationRecordStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("qualification record $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newQualificationRecordManager(store: QualificationRecordStore, clock: Clock, flags: Registry): QualificationRecordManager {
    return QualificationRecordManager(
        store = store,
        validator = defaultQualificationRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationRecordEngine(flags),
    )
}
