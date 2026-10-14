package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireSlotEngine(newRegistry())
        val item = newTireSlot("code-14", "label-820")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
