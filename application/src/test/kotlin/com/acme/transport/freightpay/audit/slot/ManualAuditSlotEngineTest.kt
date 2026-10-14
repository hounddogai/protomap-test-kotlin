package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAuditSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAuditSlotEngine(newRegistry())
        val item = newManualAuditSlot("code-909", "title-468")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
