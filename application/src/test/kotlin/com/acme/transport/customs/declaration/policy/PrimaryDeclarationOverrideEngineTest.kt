package com.acme.transport.customs.declaration.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDeclarationOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDeclarationOverrideEngine()
        val item = newPrimaryDeclarationOverride("code-904", "title-67")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
