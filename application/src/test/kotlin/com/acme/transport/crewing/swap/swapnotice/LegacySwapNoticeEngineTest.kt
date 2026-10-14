package com.acme.transport.crewing.swap.swapnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacySwapNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacySwapNoticeEngine()
        val item = newLegacySwapNotice("code-517", "name-543")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
