package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.terminal.clock.Clock

/** Moves adaptive sortation templates through their life cycle and keeps a journal of what it did. */
data class AdaptiveSortationTemplateManager(
    var store: AdaptiveSortationTemplateStore,
    var validator: AdaptiveSortationTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveSortationTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive sortation template $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveSortationTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive sortation template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveSortationTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive sortation template $code not found")
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

fun newAdaptiveSortationTemplateManager(store: AdaptiveSortationTemplateStore, clock: Clock): AdaptiveSortationTemplateManager {
    return AdaptiveSortationTemplateManager(
        store = store,
        validator = defaultAdaptiveSortationTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveSortationTemplateEngine(),
    )
}
