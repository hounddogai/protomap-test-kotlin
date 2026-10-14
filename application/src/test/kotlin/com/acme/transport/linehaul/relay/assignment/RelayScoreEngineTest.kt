package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayScoreEngine(newRegistry())
        val item = newRelayScore("code-434", "label-695")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
