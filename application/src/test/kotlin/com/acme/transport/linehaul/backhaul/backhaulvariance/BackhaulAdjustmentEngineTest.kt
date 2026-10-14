package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulAdjustmentEngine(newRegistry())
        val item = newBackhaulAdjustment("code-988", "name-173")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
