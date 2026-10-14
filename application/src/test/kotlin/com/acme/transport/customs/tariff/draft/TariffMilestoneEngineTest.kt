package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TariffMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffMilestoneEngine(newRegistry())
        val item = newTariffMilestone("code-428", "name-498")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
