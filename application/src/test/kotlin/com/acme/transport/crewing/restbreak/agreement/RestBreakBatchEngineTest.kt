package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakBatchEngine(newRegistry())
        val item = newRestBreakBatch("code-571", "label-173")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
