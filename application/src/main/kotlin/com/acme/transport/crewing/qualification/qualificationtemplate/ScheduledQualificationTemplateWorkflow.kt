package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.clock.Clock

/**
 * Moves scheduled qualification templates through their life cycle and keeps a journal of what it did.
 */
data class ScheduledQualificationTemplateManager(
    var store: ScheduledQualificationTemplateStore,
    var validator: ScheduledQualificationTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledQualificationTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled qualification template $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledQualificationTemplateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled qualification template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledQualificationTemplateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled qualification template $code not found")
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

fun newScheduledQualificationTemplateManager(store: ScheduledQualificationTemplateStore, clock: Clock): ScheduledQualificationTemplateManager {
    return ScheduledQualificationTemplateManager(
        store = store,
        validator = defaultScheduledQualificationTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledQualificationTemplateEngine(),
    )
}
