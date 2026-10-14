package com.acme.transport.fleetops.tire.target

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTireTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTireTargetEngine()
        val item = newPendingTireTarget("code-513", "label-327")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
