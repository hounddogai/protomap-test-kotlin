package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCutoffTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCutoffTargetEngine(newRegistry())
        val item = newRegionalCutoffTarget("code-381", "name-62")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
