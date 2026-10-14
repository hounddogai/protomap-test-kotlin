package com.acme.transport.linehaul.relay.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelaySummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelaySummaryEngine(newRegistry())
        val item = newRelaySummary("code-557", "name-561")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
