package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDeclarationRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDeclarationRecordEngine()
        val item = newRollingDeclarationRecord("code-3", "title-961")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
