package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRelayBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRelayBaselineEngine(newRegistry())
        val item = newManualRelayBaseline("code-385", "name-614")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
