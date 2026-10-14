package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRelayScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRelayScheduleEngine()
        val item = newCappedRelaySchedule("code-836", "title-301")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
