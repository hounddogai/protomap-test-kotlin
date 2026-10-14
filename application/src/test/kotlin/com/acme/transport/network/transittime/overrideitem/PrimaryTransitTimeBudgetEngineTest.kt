package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTransitTimeBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTransitTimeBudgetEngine(newRegistry())
        val item = newPrimaryTransitTimeBudget("code-528", "name-151")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
