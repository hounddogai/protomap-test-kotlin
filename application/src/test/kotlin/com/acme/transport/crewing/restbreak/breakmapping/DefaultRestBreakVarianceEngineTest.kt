package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRestBreakVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRestBreakVarianceEngine(newRegistry())
        val item = newDefaultRestBreakVariance("code-438", "title-642")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
