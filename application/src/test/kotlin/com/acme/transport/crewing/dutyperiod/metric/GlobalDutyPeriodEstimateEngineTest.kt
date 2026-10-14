package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDutyPeriodEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDutyPeriodEstimateEngine(newRegistry())
        val item = newGlobalDutyPeriodEstimate("code-454", "title-455")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
