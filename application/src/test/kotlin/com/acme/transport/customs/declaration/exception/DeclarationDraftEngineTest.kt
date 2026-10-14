package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationDraftEngine(newRegistry())
        val item = newDeclarationDraft("code-693", "label-628")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
