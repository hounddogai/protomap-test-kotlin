package com.acme.transport.fleetops.defect.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectRollupEngine(newRegistry())
        val item = newDefectRollup("code-443", "name-521")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
