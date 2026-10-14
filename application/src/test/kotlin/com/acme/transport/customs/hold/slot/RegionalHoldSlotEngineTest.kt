package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalHoldSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalHoldSlotEngine(newRegistry())
        val item = newRegionalHoldSlot("code-370", "name-427")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
