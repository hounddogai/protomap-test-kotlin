package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionOverrideEngine(newRegistry())
        val item = newInspectionOverride("code-778", "name-226")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
