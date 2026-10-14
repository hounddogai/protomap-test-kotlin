package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalAuditSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalAuditSlotEngine(newRegistry())
        val item = newRegionalAuditSlot("code-424", "title-700")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
