package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDeclarationReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDeclarationReviewEngine(newRegistry())
        val item = newFallbackDeclarationReview("code-37", "name-88")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
