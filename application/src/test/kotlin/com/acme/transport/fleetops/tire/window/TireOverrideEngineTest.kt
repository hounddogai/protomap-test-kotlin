package com.acme.transport.fleetops.tire.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireOverrideEngine(newRegistry())
        val item = newTireOverride("code-906", "label-457")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
