package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRouteOffsetTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRouteOffsetTargetEngine(newRegistry())
        val item = newStrictRouteOffsetTarget("code-895", "label-801")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
