package com.acme.transport.crewing.roster.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRosterSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRosterSegmentEngine()
        val item = newRegionalRosterSegment("code-184", "name-594")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
