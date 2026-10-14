package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRelayBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRelayBundleEngine()
        val item = newGlobalRelayBundle("code-388", "label-712")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
