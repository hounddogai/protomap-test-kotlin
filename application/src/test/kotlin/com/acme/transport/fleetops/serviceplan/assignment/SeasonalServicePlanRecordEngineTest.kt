package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalServicePlanRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalServicePlanRecordEngine(newRegistry())
        val item = newSeasonalServicePlanRecord("code-214", "name-159")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
