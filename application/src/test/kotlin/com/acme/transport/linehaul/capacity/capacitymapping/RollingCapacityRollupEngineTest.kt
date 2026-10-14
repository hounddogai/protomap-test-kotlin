package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCapacityRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCapacityRollupEngine()
        val item = newRollingCapacityRollup("code-839", "title-436")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
