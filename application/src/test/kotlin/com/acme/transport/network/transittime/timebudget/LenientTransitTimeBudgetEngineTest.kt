package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientTransitTimeBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientTransitTimeBudgetEngine(newRegistry())
        val item = newLenientTransitTimeBudget("code-425", "title-186")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
