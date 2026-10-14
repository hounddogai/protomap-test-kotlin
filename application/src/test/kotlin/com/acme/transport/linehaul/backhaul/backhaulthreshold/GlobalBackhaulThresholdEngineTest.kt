package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalBackhaulThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalBackhaulThresholdEngine()
        val item = newGlobalBackhaulThreshold("code-93", "name-89")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
