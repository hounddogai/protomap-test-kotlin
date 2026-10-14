package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionMappingEngine(newRegistry())
        val item = newInspectionMapping("code-182", "label-893")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
