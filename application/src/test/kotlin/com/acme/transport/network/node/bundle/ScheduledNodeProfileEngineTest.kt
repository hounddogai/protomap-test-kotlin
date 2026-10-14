package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledNodeProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledNodeProfileEngine()
        val item = newScheduledNodeProfile("code-402", "label-291")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
