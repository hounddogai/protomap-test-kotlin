package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationSlotEngine(newRegistry())
        val item = newDeclarationSlot("code-276", "title-154")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
