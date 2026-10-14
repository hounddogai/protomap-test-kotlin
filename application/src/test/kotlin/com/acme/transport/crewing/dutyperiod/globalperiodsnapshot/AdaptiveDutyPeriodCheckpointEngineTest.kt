package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDutyPeriodCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDutyPeriodCheckpointEngine()
        val item = newAdaptiveDutyPeriodCheckpoint("code-860", "title-576")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
