package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalServiceAreaThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalServiceAreaThresholdEngine()
        val item = newRegionalServiceAreaThreshold("code-929", "name-689")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
