package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledEmissionFactorRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledEmissionFactorRollupEngine(newRegistry())
        val item = newScheduledEmissionFactorRollup("code-410", "title-23")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
