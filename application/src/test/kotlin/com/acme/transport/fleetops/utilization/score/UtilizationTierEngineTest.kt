package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationTierEngine(newRegistry())
        val item = newUtilizationTier("code-861", "label-569")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
