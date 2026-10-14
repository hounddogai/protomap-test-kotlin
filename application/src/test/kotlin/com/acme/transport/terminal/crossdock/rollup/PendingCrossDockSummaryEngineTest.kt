package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCrossDockSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCrossDockSummaryEngine(newRegistry())
        val item = newPendingCrossDockSummary("code-449", "label-72")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
