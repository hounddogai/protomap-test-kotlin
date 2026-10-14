package com.acme.transport.network.transittime.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTransitTimeCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTransitTimeCycleEngine()
        val item = newGlobalTransitTimeCycle("code-937", "label-882")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
