package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapTargetEngine(newRegistry())
        val item = newSwapTarget("code-650", "name-971")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
