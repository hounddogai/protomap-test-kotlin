package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery templates through their life cycle and keeps a journal of what it did. */
data class RedeliveryTemplateService(
    var store: RedeliveryTemplateStore,
    var validator: RedeliveryTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("redelivery template $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryTemplateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryTemplateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newRedeliveryTemplateService(store: RedeliveryTemplateStore, clock: Clock): RedeliveryTemplateService {
    return RedeliveryTemplateService(
        store = store,
        validator = defaultRedeliveryTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
