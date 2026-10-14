package com.acme.transport.crewing.qualification.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedQualificationSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedQualificationSummaryEngine(newRegistry())
        val item = newBatchedQualificationSummary("code-811", "name-820")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
