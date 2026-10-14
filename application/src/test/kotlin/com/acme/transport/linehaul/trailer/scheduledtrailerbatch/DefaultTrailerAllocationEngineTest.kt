package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTrailerAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTrailerAllocationEngine()
        val item = newDefaultTrailerAllocation("code-635", "name-642")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
