package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentScheduleEngine(newRegistry())
        val item = newIncidentSchedule("code-956", "title-664")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
