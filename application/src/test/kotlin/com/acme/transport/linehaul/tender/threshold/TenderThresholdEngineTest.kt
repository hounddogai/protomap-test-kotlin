package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderThresholdEngine(newRegistry())
        val item = newTenderThreshold("code-796", "name-796")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
