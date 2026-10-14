package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceQuotaEngine(newRegistry())
        val item = newClearanceQuota("code-126", "title-320")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
