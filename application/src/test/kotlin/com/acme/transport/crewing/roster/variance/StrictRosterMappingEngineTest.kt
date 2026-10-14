package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRosterMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRosterMappingEngine(newRegistry())
        val item = newStrictRosterMapping("code-77", "label-594")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
