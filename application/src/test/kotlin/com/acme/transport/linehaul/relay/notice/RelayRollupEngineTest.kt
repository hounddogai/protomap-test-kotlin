package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayRollupEngine(newRegistry())
        val item = newRelayRollup("code-877", "title-217")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
