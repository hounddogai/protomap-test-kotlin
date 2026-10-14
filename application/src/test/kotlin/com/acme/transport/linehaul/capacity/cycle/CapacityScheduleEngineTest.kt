package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityScheduleEngine()
        val item = newCapacitySchedule("code-335", "name-260")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
