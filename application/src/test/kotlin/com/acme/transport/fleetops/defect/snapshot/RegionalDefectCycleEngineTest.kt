package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDefectCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDefectCycleEngine(newRegistry())
        val item = newRegionalDefectCycle("code-832", "label-773")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
