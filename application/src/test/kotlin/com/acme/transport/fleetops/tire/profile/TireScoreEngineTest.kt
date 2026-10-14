package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class TireScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireScoreEngine()
        val item = newTireScore("code-380", "title-717")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
