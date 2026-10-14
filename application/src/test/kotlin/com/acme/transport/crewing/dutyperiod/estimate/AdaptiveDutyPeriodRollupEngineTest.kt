package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDutyPeriodRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDutyPeriodRollupEngine(newRegistry())
        val item = newAdaptiveDutyPeriodRollup("code-975", "name-535")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
