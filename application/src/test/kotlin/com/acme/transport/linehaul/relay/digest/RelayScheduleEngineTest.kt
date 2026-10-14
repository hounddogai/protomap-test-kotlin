package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayScheduleEngine()
        val item = newRelaySchedule("code-171", "label-498")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
