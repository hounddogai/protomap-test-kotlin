package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyRestBreakMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyRestBreakMappingEngine()
        val item = newLegacyRestBreakMapping("code-834", "title-469")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
