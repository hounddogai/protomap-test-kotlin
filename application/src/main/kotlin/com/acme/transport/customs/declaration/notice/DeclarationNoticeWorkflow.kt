package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration notices through their life cycle and keeps a journal of what it did. */
data class DeclarationNoticeService(
    var store: DeclarationNoticeStore,
    var validator: DeclarationNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("declaration notice $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationNoticeStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationNoticeStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("declaration notice $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newDeclarationNoticeService(store: DeclarationNoticeStore, clock: Clock, flags: Registry): DeclarationNoticeService {
    return DeclarationNoticeService(
        store = store,
        validator = defaultDeclarationNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationNoticeEngine(flags),
    )
}
