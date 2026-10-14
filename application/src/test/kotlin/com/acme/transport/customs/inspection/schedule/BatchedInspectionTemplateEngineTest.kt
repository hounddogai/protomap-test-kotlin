package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedInspectionTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedInspectionTemplateEngine(newRegistry())
        val item = newBatchedInspectionTemplate("code-443", "name-651")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
