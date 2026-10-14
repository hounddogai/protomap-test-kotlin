package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultInspectionMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultInspectionMappingEngine(newRegistry())
        val item = newDefaultInspectionMapping("code-762", "label-412")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
