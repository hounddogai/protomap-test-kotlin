package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredInspectionGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredInspectionGroupEngine(newRegistry())
        val item = newTieredInspectionGroup("code-985", "title-822")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
