package com.acme.transport.crewing.roster.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRosterMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRosterMappingEngine()
        val item = newDefaultRosterMapping("code-86", "label-247")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
