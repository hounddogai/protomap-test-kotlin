package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualClearanceNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualClearanceNoticeEngine(newRegistry())
        val item = newManualClearanceNotice("code-239", "label-891")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
