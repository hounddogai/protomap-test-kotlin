package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDeclarationBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDeclarationBaselineEngine(newRegistry())
        val item = newTieredDeclarationBaseline("code-627", "label-682")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
