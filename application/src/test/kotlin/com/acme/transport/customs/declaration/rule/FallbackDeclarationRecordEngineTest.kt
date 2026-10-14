package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDeclarationRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDeclarationRecordEngine(newRegistry())
        val item = newFallbackDeclarationRecord("code-520", "label-324")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
