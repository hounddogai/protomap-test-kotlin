package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDeclarationWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDeclarationWindowEngine(newRegistry())
        val item = newTieredDeclarationWindow("code-460", "title-77")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
