package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalClearanceNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalClearanceNoticeEngine(newRegistry())
        val item = newSeasonalClearanceNotice("code-635", "title-314")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
