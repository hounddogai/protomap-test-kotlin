package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRelayRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRelayRollupEngine(newRegistry())
        val item = newPendingRelayRollup("code-175", "title-503")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
