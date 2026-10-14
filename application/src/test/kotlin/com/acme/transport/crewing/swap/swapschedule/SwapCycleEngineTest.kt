package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapCycleEngine(newRegistry())
        val item = newSwapCycle("code-56", "label-914")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
