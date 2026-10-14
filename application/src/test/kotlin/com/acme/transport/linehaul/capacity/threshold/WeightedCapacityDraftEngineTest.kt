package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCapacityDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCapacityDraftEngine(newRegistry())
        val item = newWeightedCapacityDraft("code-448", "title-769")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
