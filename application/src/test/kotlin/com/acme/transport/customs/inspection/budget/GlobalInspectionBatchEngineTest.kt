package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalInspectionBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalInspectionBatchEngine(newRegistry())
        val item = newGlobalInspectionBatch("code-908", "name-843")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
