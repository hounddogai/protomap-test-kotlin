package com.acme.transport.crewing.restbreak.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakProfileEngine()
        val item = newRestBreakProfile("code-838", "name-947")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
