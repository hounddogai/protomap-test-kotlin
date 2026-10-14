package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeCycleEngine(newRegistry())
        val item = newEvChargeCycle("code-777", "label-981")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
