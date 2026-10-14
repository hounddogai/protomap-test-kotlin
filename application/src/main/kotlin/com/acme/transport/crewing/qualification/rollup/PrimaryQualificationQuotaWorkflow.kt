package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary qualification quotas through their life cycle and keeps a journal of what it did. */
data class PrimaryQualificationQuotaWorkflow(
    var store: PrimaryQualificationQuotaStore,
    var validator: PrimaryQualificationQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryQualificationQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary qualification quota $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryQualificationQuotaStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary qualification quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryQualificationQuotaStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary qualification quota $code not found")
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

fun newPrimaryQualificationQuotaWorkflow(store: PrimaryQualificationQuotaStore, clock: Clock, flags: Registry): PrimaryQualificationQuotaWorkflow {
    return PrimaryQualificationQuotaWorkflow(
        store = store,
        validator = defaultPrimaryQualificationQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryQualificationQuotaEngine(flags),
    )
}
