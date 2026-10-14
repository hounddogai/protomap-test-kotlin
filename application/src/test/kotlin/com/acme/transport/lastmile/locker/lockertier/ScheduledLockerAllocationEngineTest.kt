package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLockerAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLockerAllocationEngine()
        val item = newScheduledLockerAllocation("code-641", "label-918")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
