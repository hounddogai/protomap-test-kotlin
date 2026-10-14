package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDeclarationSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDeclarationSlotEngine(newRegistry())
        val item = newRegionalDeclarationSlot("code-323", "label-822")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
