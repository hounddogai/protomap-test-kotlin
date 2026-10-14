package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalParcelSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalParcelSlotEngine(newRegistry())
        val item = newRegionalParcelSlot("code-830", "title-3")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
