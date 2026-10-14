package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDeclarationSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDeclarationSegmentEngine(newRegistry())
        val item = newAdaptiveDeclarationSegment("code-846", "name-151")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
