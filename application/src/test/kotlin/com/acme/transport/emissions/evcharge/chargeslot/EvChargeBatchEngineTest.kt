package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeBatchEngine()
        val item = newEvChargeBatch("code-217", "name-16")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
