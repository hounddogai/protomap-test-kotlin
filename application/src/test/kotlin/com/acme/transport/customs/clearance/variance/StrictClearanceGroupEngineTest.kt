package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictClearanceGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictClearanceGroupEngine()
        val item = newStrictClearanceGroup("code-214", "label-433")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
