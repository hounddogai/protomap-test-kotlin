package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationEntryEngine()
        val item = newDeclarationEntry("code-651", "title-85")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
