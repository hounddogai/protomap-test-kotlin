package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report templates through their life cycle and keeps a journal of what it did. */
data class CarbonReportTemplateManager(
    var store: CarbonReportTemplateStore,
    var validator: CarbonReportTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("carbon report template $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("carbon report template $code not found")
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

fun newCarbonReportTemplateManager(store: CarbonReportTemplateStore, clock: Clock): CarbonReportTemplateManager {
    return CarbonReportTemplateManager(
        store = store,
        validator = defaultCarbonReportTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportTemplateEngine(),
    )
}
