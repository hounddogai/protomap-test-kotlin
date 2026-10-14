package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackEvChargeEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackEvChargeEntryEngine(newRegistry())
        val item = newFallbackEvChargeEntry("code-64", "label-809")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
