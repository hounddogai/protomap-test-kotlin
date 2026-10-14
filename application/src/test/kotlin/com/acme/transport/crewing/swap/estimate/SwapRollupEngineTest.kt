package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapRollupEngine(newRegistry())
        val item = newSwapRollup("code-134", "name-245")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
