package com.acme.transport.customs.declaration.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationMetricEngine(newRegistry())
        val item = newDeclarationMetric("code-631", "title-764")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
