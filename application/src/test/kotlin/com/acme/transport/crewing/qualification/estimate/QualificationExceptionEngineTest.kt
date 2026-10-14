package com.acme.transport.crewing.qualification.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationExceptionEngine()
        val item = newQualificationException("code-361", "title-73")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
