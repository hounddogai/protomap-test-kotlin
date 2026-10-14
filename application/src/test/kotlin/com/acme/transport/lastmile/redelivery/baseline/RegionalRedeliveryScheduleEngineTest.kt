package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRedeliveryScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRedeliveryScheduleEngine(newRegistry())
        val item = newRegionalRedeliverySchedule("code-654", "label-871")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
