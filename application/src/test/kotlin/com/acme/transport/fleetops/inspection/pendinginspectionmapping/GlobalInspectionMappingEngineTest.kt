package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalInspectionMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalInspectionMappingEngine(newRegistry())
        val item = newGlobalInspectionMapping("code-524", "name-275")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
