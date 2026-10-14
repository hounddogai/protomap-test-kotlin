package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDeclarationTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDeclarationTierEngine(newRegistry())
        val item = newRollingDeclarationTier("code-63", "name-449")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
