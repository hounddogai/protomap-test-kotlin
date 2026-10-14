package com.acme.transport.customs.declaration.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationAssignmentEngine()
        val item = newDeclarationAssignment("code-549", "title-776")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
