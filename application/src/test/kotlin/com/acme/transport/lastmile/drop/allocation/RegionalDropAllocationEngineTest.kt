package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDropAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDropAllocationEngine()
        val item = newRegionalDropAllocation("code-555", "name-374")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
