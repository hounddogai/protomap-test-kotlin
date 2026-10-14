package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryEvChargeGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryEvChargeGroupEngine(newRegistry())
        val item = newPrimaryEvChargeGroup("code-238", "name-766")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
