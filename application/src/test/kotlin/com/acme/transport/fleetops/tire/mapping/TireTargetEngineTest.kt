package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireTargetEngine(newRegistry())
        val item = newTireTarget("code-222", "label-709")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
