package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeProfileEngine(newRegistry())
        val item = newEvChargeProfile("code-924", "name-756")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
