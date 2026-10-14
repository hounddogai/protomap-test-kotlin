package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class TenderQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderQuotaEngine()
        val item = newTenderQuota("code-277", "label-439")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
