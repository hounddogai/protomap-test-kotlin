package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerAdjustmentEngine()
        val item = newTrailerAdjustment("code-451", "title-968")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
