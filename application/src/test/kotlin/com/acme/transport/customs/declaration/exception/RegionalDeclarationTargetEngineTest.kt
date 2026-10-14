package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDeclarationTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDeclarationTargetEngine()
        val item = newRegionalDeclarationTarget("code-719", "name-11")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
