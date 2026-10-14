package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCrossDockSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCrossDockSummaryEngine(newRegistry())
        val item = newAdaptiveCrossDockSummary("code-865", "name-300")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
