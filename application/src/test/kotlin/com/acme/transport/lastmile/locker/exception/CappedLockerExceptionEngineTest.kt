package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLockerExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLockerExceptionEngine()
        val item = newCappedLockerException("code-493", "label-540")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
