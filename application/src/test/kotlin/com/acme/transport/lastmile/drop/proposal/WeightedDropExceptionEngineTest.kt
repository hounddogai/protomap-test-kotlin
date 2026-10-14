package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDropExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDropExceptionEngine(newRegistry())
        val item = newWeightedDropException("code-195", "title-495")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
