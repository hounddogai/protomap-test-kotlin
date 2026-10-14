package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDeclarationTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDeclarationTargetEngine(newRegistry())
        val item = newFallbackDeclarationTarget("code-779", "title-619")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
