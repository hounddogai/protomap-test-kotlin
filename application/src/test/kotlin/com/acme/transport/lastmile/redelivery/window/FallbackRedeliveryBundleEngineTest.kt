package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRedeliveryBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRedeliveryBundleEngine()
        val item = newFallbackRedeliveryBundle("code-402", "label-601")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
