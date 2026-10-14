package com.acme.transport.network.servicearea.rollup

import com.acme.transport.network.clock.Clock

/** Moves service area templates through their life cycle and keeps a journal of what it did. */
data class ServiceAreaTemplateService(
    var store: ServiceAreaTemplateStore,
    var validator: ServiceAreaTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("service area template $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaTemplateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service area template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaTemplateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newServiceAreaTemplateService(store: ServiceAreaTemplateStore, clock: Clock): ServiceAreaTemplateService {
    return ServiceAreaTemplateService(
        store = store,
        validator = defaultServiceAreaTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
