package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeTargetEngine(newRegistry())
        val item = newTransitTimeTarget("code-949", "label-159")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
