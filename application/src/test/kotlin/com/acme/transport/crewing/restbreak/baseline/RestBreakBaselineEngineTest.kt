package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakBaselineEngine(newRegistry())
        val item = newRestBreakBaseline("code-636", "name-173")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
