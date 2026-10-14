package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingNodeBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingNodeBundleEngine(newRegistry())
        val item = newPendingNodeBundle("code-849", "name-751")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
