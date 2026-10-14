package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCrossDockSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCrossDockSlotEngine(newRegistry())
        val item = newWeightedCrossDockSlot("code-854", "name-938")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
