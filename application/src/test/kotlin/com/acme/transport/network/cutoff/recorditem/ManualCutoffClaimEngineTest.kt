package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCutoffClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCutoffClaimEngine()
        val item = newManualCutoffClaim("code-894", "label-92")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
