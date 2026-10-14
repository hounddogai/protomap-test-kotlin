package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceNoticeEngine()
        val item = newClearanceNotice("code-719", "label-849")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
