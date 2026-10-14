package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeRollupEngine(newRegistry())
        val item = newEvChargeRollup("code-580", "name-765")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
