package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCapacityTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCapacityTargetEngine(newRegistry())
        val item = newRegionalCapacityTarget("code-421", "name-721")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
