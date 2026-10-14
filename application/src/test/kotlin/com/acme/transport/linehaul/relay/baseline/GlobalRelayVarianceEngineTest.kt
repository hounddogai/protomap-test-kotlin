package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRelayVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRelayVarianceEngine(newRegistry())
        val item = newGlobalRelayVariance("code-653", "label-136")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
