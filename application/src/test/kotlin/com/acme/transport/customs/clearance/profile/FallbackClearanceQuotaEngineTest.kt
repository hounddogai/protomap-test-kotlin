package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackClearanceQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackClearanceQuotaEngine(newRegistry())
        val item = newFallbackClearanceQuota("code-520", "label-369")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
