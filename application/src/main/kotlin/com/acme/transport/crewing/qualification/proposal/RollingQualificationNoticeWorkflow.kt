package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.crewing.clock.Clock

/** Moves rolling qualification notices through their life cycle and keeps a journal of what it did. */
data class RollingQualificationNoticeService(
    var store: RollingQualificationNoticeStore,
    var validator: RollingQualificationNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingQualificationNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling qualification notice $code not found")
        validator.requireValid(item)
        item.moveTo(RollingQualificationNoticeStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling qualification notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingQualificationNoticeStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling qualification notice $code not found")
        val result = engine.compute(item)
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

fun newRollingQualificationNoticeService(store: RollingQualificationNoticeStore, clock: Clock): RollingQualificationNoticeService {
    return RollingQualificationNoticeService(
        store = store,
        validator = defaultRollingQualificationNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingQualificationNoticeEngine(),
    )
}
