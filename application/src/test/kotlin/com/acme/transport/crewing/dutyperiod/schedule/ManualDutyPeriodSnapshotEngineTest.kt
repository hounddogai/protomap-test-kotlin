package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDutyPeriodSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDutyPeriodSnapshotEngine(newRegistry())
        val item = newManualDutyPeriodSnapshot("code-837", "name-527")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
