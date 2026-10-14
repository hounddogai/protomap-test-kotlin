package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.crewing.clock.Clock

/** Moves qualification manifests through their life cycle and keeps a journal of what it did. */
data class QualificationManifestManager(
    var store: QualificationManifestStore,
    var validator: QualificationManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("qualification manifest $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("qualification manifest $code not found")
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

fun newQualificationManifestManager(store: QualificationManifestStore, clock: Clock): QualificationManifestManager {
    return QualificationManifestManager(
        store = store,
        validator = defaultQualificationManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationManifestEngine(),
    )
}
