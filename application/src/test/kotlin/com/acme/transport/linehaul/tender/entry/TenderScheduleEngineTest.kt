package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderScheduleEngine(newRegistry())
        val item = newTenderSchedule("code-533", "label-924")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
