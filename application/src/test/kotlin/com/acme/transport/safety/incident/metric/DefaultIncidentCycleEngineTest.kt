package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultIncidentCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultIncidentCycleEngine(newRegistry())
        val item = newDefaultIncidentCycle("code-245", "name-99")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
