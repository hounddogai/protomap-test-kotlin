package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerExceptionEngine()
        val item = newLockerException("code-479", "name-401")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
