package com.acme.transport.customs.declaration.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDeclarationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDeclarationPolicyEngine(newRegistry())
        val item = newLenientDeclarationPolicy("code-280", "name-209")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
