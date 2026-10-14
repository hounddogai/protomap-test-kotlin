package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDutyPeriodPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDutyPeriodPolicyEngine(newRegistry())
        val item = newCappedDutyPeriodPolicy("code-352", "label-99")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
