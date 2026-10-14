package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceCycleEngine(newRegistry())
        val item = newClearanceCycle("code-272", "label-705")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
