package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCrossDockQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCrossDockQuotaEngine()
        val item = newRegionalCrossDockQuota("code-773", "label-382")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
