package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryIncidentQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryIncidentQuotaEngine()
        val item = newPrimaryIncidentQuota("code-608", "name-904")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
