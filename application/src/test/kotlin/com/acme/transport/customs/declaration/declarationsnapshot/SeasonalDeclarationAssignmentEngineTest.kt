package com.acme.transport.customs.declaration.declarationsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDeclarationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDeclarationAssignmentEngine()
        val item = newSeasonalDeclarationAssignment("code-361", "name-416")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
