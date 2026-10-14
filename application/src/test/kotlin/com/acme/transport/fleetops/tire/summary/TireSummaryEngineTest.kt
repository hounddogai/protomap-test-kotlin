package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireSummaryEngine(newRegistry())
        val item = newTireSummary("code-874", "title-938")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
