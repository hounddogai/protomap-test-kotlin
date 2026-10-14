package com.acme.transport.crewing.qualification.review

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationReviewEngine()
        val item = newQualificationReview("code-791", "name-541")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
