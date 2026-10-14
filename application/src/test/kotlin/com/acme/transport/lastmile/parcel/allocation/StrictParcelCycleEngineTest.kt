package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictParcelCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictParcelCycleEngine(newRegistry())
        val item = newStrictParcelCycle("code-972", "name-643")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
