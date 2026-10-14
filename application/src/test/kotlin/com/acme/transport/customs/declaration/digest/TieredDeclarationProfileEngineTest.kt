package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDeclarationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDeclarationProfileEngine()
        val item = newTieredDeclarationProfile("code-281", "title-869")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
