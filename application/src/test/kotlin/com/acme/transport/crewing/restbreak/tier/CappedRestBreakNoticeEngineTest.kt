package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRestBreakNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRestBreakNoticeEngine()
        val item = newCappedRestBreakNotice("code-732", "name-139")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
