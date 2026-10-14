package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDeclarationDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDeclarationDigestEngine(newRegistry())
        val item = newPrimaryDeclarationDigest("code-133", "title-116")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
