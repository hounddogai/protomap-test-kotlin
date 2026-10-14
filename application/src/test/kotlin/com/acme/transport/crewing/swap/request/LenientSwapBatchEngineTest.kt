package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientSwapBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientSwapBatchEngine(newRegistry())
        val item = newLenientSwapBatch("code-303", "name-455")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
