package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyInspectionClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyInspectionClaimEngine(newRegistry())
        val item = newLegacyInspectionClaim("code-949", "title-797")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
