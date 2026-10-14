package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDeclarationReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDeclarationReviewEngine(newRegistry())
        val item = newSeasonalDeclarationReview("code-962", "name-241")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
