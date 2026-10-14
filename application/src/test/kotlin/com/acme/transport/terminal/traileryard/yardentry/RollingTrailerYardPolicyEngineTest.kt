package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTrailerYardPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTrailerYardPolicyEngine()
        val item = newRollingTrailerYardPolicy("code-884", "title-318")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
