package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientRedeliverySnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientRedeliverySnapshotEngine()
        val item = newLenientRedeliverySnapshot("code-209", "label-257")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
