package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRestBreakExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRestBreakExceptionEngine(newRegistry())
        val item = newTieredRestBreakException("code-378", "label-763")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
