package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakMappingEngine(newRegistry())
        val item = newRestBreakMapping("code-499", "name-168")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
