package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTenderBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTenderBatchEngine(newRegistry())
        val item = newSeasonalTenderBatch("code-101", "title-655")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
