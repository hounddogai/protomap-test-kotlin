package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryHoldAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryHoldAdjustmentEngine(newRegistry())
        val item = newPrimaryHoldAdjustment("code-141", "name-59")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
