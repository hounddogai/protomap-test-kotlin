package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report rollups through their life cycle and keeps a journal of what it did. */
data class CarbonReportRollupCoordinator(
    var store: CarbonReportRollupStore,
    var validator: CarbonReportRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("carbon report rollup $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("carbon report rollup $code not found")
        val result = engine.price(item)
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

fun newCarbonReportRollupCoordinator(store: CarbonReportRollupStore, clock: Clock): CarbonReportRollupCoordinator {
    return CarbonReportRollupCoordinator(
        store = store,
        validator = defaultCarbonReportRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportRollupEngine(),
    )
}
