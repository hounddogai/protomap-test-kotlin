package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class TireProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireProfileEngine()
        val item = newTireProfile("code-671", "name-614")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
