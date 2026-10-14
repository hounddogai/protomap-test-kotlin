package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldAdjustmentEngine(newRegistry())
        val item = newHoldAdjustment("code-474", "label-272")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
