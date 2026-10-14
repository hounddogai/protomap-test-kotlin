package com.acme.transport.emissions.idling.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledIdlingAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledIdlingAuditEngine(newRegistry())
        val item = newScheduledIdlingAudit("code-177", "label-702")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
