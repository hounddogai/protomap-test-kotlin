package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalZoneTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalZoneTierEngine(newRegistry())
        val item = newRegionalZoneTier("code-642", "label-610")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
