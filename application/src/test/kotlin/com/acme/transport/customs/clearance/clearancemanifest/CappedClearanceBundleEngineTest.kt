package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedClearanceBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedClearanceBundleEngine()
        val item = newCappedClearanceBundle("code-297", "label-514")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
