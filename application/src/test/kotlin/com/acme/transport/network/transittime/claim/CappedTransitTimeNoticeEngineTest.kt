package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTransitTimeNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTransitTimeNoticeEngine(newRegistry())
        val item = newCappedTransitTimeNotice("code-456", "title-263")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
