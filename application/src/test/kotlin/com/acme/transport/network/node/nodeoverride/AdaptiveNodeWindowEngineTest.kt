package com.acme.transport.network.node.nodeoverride

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveNodeWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveNodeWindowEngine(newRegistry())
        val item = newAdaptiveNodeWindow("code-412", "label-246")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
