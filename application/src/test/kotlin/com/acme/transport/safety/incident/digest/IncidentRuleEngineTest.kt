package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class IncidentRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIncidentRuleEngine()
        val item = newIncidentRule("code-805", "label-513")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
