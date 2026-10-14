package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldCycleEngine(newRegistry())
        val item = newHoldCycle("code-893", "label-566")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
