package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFuelBurnOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFuelBurnOverrideEngine(newRegistry())
        val item = newRollingFuelBurnOverride("code-21", "name-190")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
