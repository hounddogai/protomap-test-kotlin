package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationCycleEngine(newRegistry())
        val item = newDeclarationCycle("code-710", "name-320")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
