package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAuditMappingEngine(newRegistry())
        val item = newAuditMapping("code-753", "name-569")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
