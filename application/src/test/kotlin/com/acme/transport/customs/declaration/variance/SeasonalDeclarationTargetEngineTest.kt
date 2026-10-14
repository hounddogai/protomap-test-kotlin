package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDeclarationTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDeclarationTargetEngine()
        val item = newSeasonalDeclarationTarget("code-981", "title-181")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
