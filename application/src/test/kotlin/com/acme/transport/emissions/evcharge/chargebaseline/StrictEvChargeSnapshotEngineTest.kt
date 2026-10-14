package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictEvChargeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictEvChargeSnapshotEngine(newRegistry())
        val item = newStrictEvChargeSnapshot("code-850", "name-45")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
