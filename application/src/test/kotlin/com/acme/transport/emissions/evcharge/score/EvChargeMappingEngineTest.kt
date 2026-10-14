package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeMappingEngine(newRegistry())
        val item = newEvChargeMapping("code-566", "name-912")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
