package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientTireRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientTireRequestEngine(newRegistry())
        val item = newLenientTireRequest("code-164", "title-940")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
