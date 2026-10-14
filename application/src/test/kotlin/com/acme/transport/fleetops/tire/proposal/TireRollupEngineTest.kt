package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireRollupEngine(newRegistry())
        val item = newTireRollup("code-966", "label-745")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
