package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryScheduleEngine(newRegistry())
        val item = newRedeliverySchedule("code-123", "name-818")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
