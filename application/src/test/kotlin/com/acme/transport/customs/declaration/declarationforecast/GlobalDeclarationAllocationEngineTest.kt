package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDeclarationAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDeclarationAllocationEngine(newRegistry())
        val item = newGlobalDeclarationAllocation("code-23", "title-588")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
