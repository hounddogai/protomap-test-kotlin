package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling qualification digests through their life cycle and keeps a journal of what it did. */
data class RollingQualificationDigestService(
    var store: RollingQualificationDigestStore,
    var validator: RollingQualificationDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingQualificationDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling qualification digest $code not found")
        validator.requireValid(item)
        item.moveTo(RollingQualificationDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling qualification digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingQualificationDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling qualification digest $code not found")
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

fun newRollingQualificationDigestService(store: RollingQualificationDigestStore, clock: Clock, flags: Registry): RollingQualificationDigestService {
    return RollingQualificationDigestService(
        store = store,
        validator = defaultRollingQualificationDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingQualificationDigestEngine(flags),
    )
}
