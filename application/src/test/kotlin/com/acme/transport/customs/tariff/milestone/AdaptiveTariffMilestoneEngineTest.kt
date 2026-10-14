package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveTariffMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveTariffMilestoneEngine(newRegistry())
        val item = newAdaptiveTariffMilestone("code-175", "label-539")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
