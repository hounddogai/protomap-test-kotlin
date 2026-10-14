package com.acme.transport.customs.clearance.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceBaselineEngine()
        val item = newClearanceBaseline("code-483", "title-632")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
