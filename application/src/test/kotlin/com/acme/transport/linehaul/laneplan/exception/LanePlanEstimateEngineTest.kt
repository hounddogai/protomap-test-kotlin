package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanEstimateEngine(newRegistry())
        val item = newLanePlanEstimate("code-425", "label-234")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
