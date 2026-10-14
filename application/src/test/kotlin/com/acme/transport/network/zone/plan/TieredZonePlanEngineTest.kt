package com.acme.transport.network.zone.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredZonePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredZonePlanEngine(newRegistry())
        val item = newTieredZonePlan("code-194", "title-481")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
