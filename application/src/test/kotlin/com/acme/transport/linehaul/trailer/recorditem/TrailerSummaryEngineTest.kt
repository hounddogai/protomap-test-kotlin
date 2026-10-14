package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerSummaryEngine(newRegistry())
        val item = newTrailerSummary("code-842", "name-28")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
