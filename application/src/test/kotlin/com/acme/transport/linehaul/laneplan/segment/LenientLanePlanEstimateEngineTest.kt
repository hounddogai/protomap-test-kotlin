package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientLanePlanEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientLanePlanEstimateEngine(newRegistry())
        val item = newLenientLanePlanEstimate("code-791", "title-945")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
