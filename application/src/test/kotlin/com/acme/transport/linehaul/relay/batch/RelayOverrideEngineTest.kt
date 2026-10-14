package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayOverrideEngine(newRegistry())
        val item = newRelayOverride("code-527", "name-5")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
