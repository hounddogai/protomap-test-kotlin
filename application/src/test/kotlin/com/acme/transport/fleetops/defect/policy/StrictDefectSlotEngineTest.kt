package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDefectSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDefectSlotEngine(newRegistry())
        val item = newStrictDefectSlot("code-314", "name-575")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
