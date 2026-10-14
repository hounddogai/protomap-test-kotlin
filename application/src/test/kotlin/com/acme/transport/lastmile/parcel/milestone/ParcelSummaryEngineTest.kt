package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ParcelSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultParcelSummaryEngine(newRegistry())
        val item = newParcelSummary("code-150", "title-219")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
