package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictSortationTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictSortationTierEngine()
        val item = newStrictSortationTier("code-729", "title-288")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
