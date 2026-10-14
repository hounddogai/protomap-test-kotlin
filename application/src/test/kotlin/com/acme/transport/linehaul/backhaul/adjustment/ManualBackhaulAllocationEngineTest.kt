package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualBackhaulAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualBackhaulAllocationEngine(newRegistry())
        val item = newManualBackhaulAllocation("code-656", "name-725")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
