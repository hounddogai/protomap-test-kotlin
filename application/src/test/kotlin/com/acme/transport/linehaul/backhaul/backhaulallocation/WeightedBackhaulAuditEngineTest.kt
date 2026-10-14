package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedBackhaulAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedBackhaulAuditEngine(newRegistry())
        val item = newWeightedBackhaulAudit("code-447", "name-237")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
