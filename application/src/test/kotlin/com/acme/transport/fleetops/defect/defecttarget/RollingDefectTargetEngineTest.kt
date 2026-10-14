package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDefectTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDefectTargetEngine(newRegistry())
        val item = newRollingDefectTarget("code-803", "title-361")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
