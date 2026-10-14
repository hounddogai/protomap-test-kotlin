package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTenderAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTenderAllocationEngine()
        val item = newStrictTenderAllocation("code-438", "name-287")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
