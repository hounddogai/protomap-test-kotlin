package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakSummaryEngine(newRegistry())
        val item = newRestBreakSummary("code-705", "name-506")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
