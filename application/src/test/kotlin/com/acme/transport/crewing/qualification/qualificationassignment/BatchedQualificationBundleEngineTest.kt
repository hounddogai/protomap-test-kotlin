package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedQualificationBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedQualificationBundleEngine(newRegistry())
        val item = newBatchedQualificationBundle("code-704", "label-462")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
