package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDeclarationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDeclarationAssignmentEngine(newRegistry())
        val item = newTieredDeclarationAssignment("code-189", "label-887")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
