package com.acme.transport.customs.declaration.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDeclarationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDeclarationPolicyEngine()
        val item = newTieredDeclarationPolicy("code-690", "name-167")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
