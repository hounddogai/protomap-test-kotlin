package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDeclarationBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDeclarationBudgetEngine()
        val item = newFallbackDeclarationBudget("code-245", "name-652")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
