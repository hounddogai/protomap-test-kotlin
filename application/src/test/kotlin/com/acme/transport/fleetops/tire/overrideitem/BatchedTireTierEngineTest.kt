package com.acme.transport.fleetops.tire.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTireTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTireTierEngine()
        val item = newBatchedTireTier("code-163", "name-36")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
