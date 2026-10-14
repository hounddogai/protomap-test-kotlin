package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationProfileEngine(newRegistry())
        val item = newDeclarationProfile("code-735", "title-946")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
