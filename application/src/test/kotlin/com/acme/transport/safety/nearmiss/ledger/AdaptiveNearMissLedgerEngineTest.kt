package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveNearMissLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveNearMissLedgerEngine(newRegistry())
        val item = newAdaptiveNearMissLedger("code-503", "label-788")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
