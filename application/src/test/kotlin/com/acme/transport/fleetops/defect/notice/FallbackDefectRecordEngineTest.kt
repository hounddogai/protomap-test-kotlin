package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDefectRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDefectRecordEngine()
        val item = newFallbackDefectRecord("code-259", "label-905")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
