package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDeclarationMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDeclarationMetricEngine()
        val item = newLenientDeclarationMetric("code-486", "title-793")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
