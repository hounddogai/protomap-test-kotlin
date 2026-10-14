package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDeclarationGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDeclarationGroupEngine()
        val item = newRollingDeclarationGroup("code-13", "title-914")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
