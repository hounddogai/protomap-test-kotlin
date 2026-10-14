package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditMetricEngine(newRegistry())
        val item = newAuditMetric("code-82", "title-601")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
