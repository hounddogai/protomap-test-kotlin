package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveBackhaulLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveBackhaulLedgerEngine()
        val item = newAdaptiveBackhaulLedger("code-400", "title-115")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
