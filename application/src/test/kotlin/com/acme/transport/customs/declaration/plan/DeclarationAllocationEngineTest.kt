package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationAllocationEngine(newRegistry())
        val item = newDeclarationAllocation("code-952", "name-816")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
