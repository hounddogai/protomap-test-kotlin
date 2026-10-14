package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalClearanceOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalClearanceOverrideEngine()
        val item = newRegionalClearanceOverride("code-111", "label-610")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
