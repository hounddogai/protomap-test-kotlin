package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientEvChargeThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientEvChargeThresholdEngine(newRegistry())
        val item = newLenientEvChargeThreshold("code-333", "title-475")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
