package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledIdlingVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledIdlingVarianceEngine(newRegistry())
        val item = newScheduledIdlingVariance("code-826", "title-622")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
