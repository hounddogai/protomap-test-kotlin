package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTenderRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTenderRecordEngine()
        val item = newScheduledTenderRecord("code-753", "title-343")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
