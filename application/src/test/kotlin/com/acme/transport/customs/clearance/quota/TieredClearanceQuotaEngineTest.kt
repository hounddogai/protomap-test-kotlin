package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredClearanceQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredClearanceQuotaEngine(newRegistry())
        val item = newTieredClearanceQuota("code-366", "name-390")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
