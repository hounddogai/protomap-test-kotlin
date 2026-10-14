package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.terminal.clock.Clock

/** Moves global dock door templates through their life cycle and keeps a journal of what it did. */
data class GlobalDockDoorTemplateService(
    var store: GlobalDockDoorTemplateStore,
    var validator: GlobalDockDoorTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalDockDoorTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global dock door template $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDockDoorTemplateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global dock door template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDockDoorTemplateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global dock door template $code not found")
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

fun newGlobalDockDoorTemplateService(store: GlobalDockDoorTemplateStore, clock: Clock): GlobalDockDoorTemplateService {
    return GlobalDockDoorTemplateService(
        store = store,
        validator = defaultGlobalDockDoorTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalDockDoorTemplateEngine(),
    )
}
