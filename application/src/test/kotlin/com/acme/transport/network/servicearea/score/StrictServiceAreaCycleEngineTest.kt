package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictServiceAreaCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictServiceAreaCycleEngine()
        val item = newStrictServiceAreaCycle("code-793", "title-379")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
