package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDeclarationEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDeclarationEntryEngine(newRegistry())
        val item = newWeightedDeclarationEntry("code-343", "title-583")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
