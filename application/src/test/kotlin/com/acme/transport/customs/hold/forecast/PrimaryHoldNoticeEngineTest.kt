package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryHoldNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryHoldNoticeEngine()
        val item = newPrimaryHoldNotice("code-813", "name-235")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
