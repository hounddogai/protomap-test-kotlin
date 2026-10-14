package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalInspectionGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalInspectionGroupEngine(newRegistry())
        val item = newGlobalInspectionGroup("code-730", "name-355")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
