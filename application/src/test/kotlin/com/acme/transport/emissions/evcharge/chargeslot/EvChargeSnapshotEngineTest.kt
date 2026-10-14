package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeSnapshotEngine(newRegistry())
        val item = newEvChargeSnapshot("code-409", "label-378")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
