package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive service plan notices through their life cycle and keeps a journal of what it did. */
data class AdaptiveServicePlanNoticeService(
    var store: AdaptiveServicePlanNoticeStore,
    var validator: AdaptiveServicePlanNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveServicePlanNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive service plan notice $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveServicePlanNoticeStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive service plan notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveServicePlanNoticeStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive service plan notice $code not found")
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

fun newAdaptiveServicePlanNoticeService(store: AdaptiveServicePlanNoticeStore, clock: Clock): AdaptiveServicePlanNoticeService {
    return AdaptiveServicePlanNoticeService(
        store = store,
        validator = defaultAdaptiveServicePlanNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveServicePlanNoticeEngine(),
    )
}
