package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDutyPeriodNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDutyPeriodNoticeEngine(newRegistry())
        val item = newManualDutyPeriodNotice("code-147", "label-252")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
