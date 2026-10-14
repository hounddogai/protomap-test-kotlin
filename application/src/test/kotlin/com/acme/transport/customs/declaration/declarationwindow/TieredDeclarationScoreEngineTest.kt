package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDeclarationScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDeclarationScoreEngine()
        val item = newTieredDeclarationScore("code-650", "label-48")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
