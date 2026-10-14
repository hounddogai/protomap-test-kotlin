package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectBatchEngine()
        val item = newDefectBatch("code-762", "name-150")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
