package com.acme.transport.customs.declaration.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDeclarationReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDeclarationReviewEngine()
        val item = newDefaultDeclarationReview("code-941", "name-180")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
