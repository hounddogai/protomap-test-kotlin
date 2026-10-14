package com.acme.transport.crewing.restbreak.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakOverrideEngine()
        val item = newRestBreakOverride("code-631", "name-125")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
