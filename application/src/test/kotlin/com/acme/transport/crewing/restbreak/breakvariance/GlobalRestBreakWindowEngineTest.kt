package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRestBreakWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRestBreakWindowEngine()
        val item = newGlobalRestBreakWindow("code-755", "title-474")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
