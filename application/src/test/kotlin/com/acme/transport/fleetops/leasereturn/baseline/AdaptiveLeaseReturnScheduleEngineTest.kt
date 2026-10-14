package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLeaseReturnScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLeaseReturnScheduleEngine()
        val item = newAdaptiveLeaseReturnSchedule("code-622", "label-105")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
