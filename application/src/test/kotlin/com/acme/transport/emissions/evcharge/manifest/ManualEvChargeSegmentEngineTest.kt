package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualEvChargeSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualEvChargeSegmentEngine()
        val item = newManualEvChargeSegment("code-189", "name-733")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
