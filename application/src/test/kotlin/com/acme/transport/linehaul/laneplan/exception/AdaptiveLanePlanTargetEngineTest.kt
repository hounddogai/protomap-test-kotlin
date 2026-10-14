package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLanePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLanePlanTargetEngine(newRegistry())
        val item = newAdaptiveLanePlanTarget("code-252", "name-794")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
