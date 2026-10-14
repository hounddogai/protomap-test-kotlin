package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTireBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTireBaselineEngine(newRegistry())
        val item = newPendingTireBaseline("code-475", "label-671")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
