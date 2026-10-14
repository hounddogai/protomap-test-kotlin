package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCrossDockQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCrossDockQuotaEngine()
        val item = newLenientCrossDockQuota("code-376", "name-455")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
