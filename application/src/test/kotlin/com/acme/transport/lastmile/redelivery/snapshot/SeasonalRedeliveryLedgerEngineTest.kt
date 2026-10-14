package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRedeliveryLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRedeliveryLedgerEngine(newRegistry())
        val item = newSeasonalRedeliveryLedger("code-580", "label-920")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
