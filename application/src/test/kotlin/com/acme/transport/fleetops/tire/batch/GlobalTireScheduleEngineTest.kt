package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTireScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTireScheduleEngine(newRegistry())
        val item = newGlobalTireSchedule("code-630", "name-328")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
