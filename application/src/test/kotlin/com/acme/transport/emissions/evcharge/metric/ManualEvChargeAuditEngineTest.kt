package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualEvChargeAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualEvChargeAuditEngine(newRegistry())
        val item = newManualEvChargeAudit("code-491", "title-886")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
