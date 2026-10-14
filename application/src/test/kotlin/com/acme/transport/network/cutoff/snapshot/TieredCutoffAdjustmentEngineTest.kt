package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCutoffAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCutoffAdjustmentEngine(newRegistry())
        val item = newTieredCutoffAdjustment("code-504", "label-687")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
