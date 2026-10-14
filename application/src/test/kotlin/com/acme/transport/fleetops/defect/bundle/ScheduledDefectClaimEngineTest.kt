package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDefectClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDefectClaimEngine(newRegistry())
        val item = newScheduledDefectClaim("code-10", "title-418")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
