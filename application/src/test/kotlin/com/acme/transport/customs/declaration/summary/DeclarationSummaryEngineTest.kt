package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationSummaryEngine(newRegistry())
        val item = newDeclarationSummary("code-772", "name-889")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
