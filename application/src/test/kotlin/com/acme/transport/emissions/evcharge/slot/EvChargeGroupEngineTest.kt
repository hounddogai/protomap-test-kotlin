package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeGroupEngine(newRegistry())
        val item = newEvChargeGroup("code-507", "title-132")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
