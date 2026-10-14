package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryLeaseReturnScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryLeaseReturnScheduleEngine(newRegistry())
        val item = newPrimaryLeaseReturnSchedule("code-624", "label-196")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
