package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeNoticeEngine()
        val item = newTransitTimeNotice("code-742", "title-945")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
