package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapEntryEngine(newRegistry())
        val item = newSwapEntry("code-713", "name-210")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
