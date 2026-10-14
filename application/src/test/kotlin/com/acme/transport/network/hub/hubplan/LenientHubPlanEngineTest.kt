package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientHubPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientHubPlanEngine(newRegistry())
        val item = newLenientHubPlan("code-362", "title-569")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
