package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeVarianceEngine(newRegistry())
        val item = newEvChargeVariance("code-639", "name-984")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
