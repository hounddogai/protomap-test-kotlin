package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalEvChargeOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalEvChargeOverrideEngine(newRegistry())
        val item = newGlobalEvChargeOverride("code-677", "name-98")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
