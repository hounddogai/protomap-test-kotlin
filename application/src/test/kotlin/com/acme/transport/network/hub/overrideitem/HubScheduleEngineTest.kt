package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class HubScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubScheduleEngine()
        val item = newHubSchedule("code-100", "title-895")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
