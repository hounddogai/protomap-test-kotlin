package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedQualificationCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedQualificationCycleEngine()
        val item = newBatchedQualificationCycle("code-337", "title-903")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
