package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionNoticeEngine(newRegistry())
        val item = newInspectionNotice("code-661", "name-105")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
