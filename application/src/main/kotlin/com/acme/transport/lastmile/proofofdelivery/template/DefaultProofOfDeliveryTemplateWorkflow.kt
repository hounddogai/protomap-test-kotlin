package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves default proof of delivery templates through their life cycle and keeps a journal of what it did.
 */
data class DefaultProofOfDeliveryTemplateWorkflow(
    var store: DefaultProofOfDeliveryTemplateStore,
    var validator: DefaultProofOfDeliveryTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultProofOfDeliveryTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default proof of delivery template $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultProofOfDeliveryTemplateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default proof of delivery template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultProofOfDeliveryTemplateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default proof of delivery template $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefaultProofOfDeliveryTemplateWorkflow(store: DefaultProofOfDeliveryTemplateStore, clock: Clock): DefaultProofOfDeliveryTemplateWorkflow {
    return DefaultProofOfDeliveryTemplateWorkflow(
        store = store,
        validator = defaultDefaultProofOfDeliveryTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultProofOfDeliveryTemplateEngine(),
    )
}
