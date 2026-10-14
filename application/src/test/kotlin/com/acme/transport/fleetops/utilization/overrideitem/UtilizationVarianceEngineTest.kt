package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationVarianceEngine(newRegistry())
        val item = newUtilizationVariance("code-776", "name-216")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
