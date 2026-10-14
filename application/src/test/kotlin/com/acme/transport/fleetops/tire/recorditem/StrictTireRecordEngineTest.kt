package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTireRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTireRecordEngine()
        val item = newStrictTireRecord("code-476", "label-53")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
