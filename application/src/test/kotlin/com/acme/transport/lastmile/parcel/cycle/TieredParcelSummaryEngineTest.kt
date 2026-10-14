package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredParcelSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredParcelSummaryEngine(newRegistry())
        val item = newTieredParcelSummary("code-529", "name-242")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
