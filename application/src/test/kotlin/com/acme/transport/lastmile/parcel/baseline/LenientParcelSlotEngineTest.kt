package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientParcelSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientParcelSlotEngine(newRegistry())
        val item = newLenientParcelSlot("code-633", "name-861")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
