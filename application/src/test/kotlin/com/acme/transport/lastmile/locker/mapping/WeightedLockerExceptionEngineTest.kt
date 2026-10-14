package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedLockerExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedLockerExceptionEngine(newRegistry())
        val item = newWeightedLockerException("code-648", "name-601")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
