package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldCheckpointEngine()
        val item = newHoldCheckpoint("code-720", "name-446")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
