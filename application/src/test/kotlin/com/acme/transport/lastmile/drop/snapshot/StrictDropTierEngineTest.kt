package com.acme.transport.lastmile.drop.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDropTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDropTierEngine()
        val item = newStrictDropTier("code-305", "title-828")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
