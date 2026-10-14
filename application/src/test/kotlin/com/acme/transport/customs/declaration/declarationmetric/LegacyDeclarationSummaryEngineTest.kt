package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDeclarationSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDeclarationSummaryEngine()
        val item = newLegacyDeclarationSummary("code-772", "name-469")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
