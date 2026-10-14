package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRestBreakLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRestBreakLedgerEngine()
        val item = newScheduledRestBreakLedger("code-263", "label-392")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
