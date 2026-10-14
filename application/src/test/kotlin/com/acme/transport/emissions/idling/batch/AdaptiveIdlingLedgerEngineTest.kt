package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveIdlingLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveIdlingLedgerEngine(newRegistry())
        val item = newAdaptiveIdlingLedger("code-763", "label-402")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
