package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingAuditMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingAuditMappingEngine(newRegistry())
        val item = newRollingAuditMapping("code-734", "name-485")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
