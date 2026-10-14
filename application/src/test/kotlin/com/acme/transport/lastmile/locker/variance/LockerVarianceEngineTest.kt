package com.acme.transport.lastmile.locker.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerVarianceEngine()
        val item = newLockerVariance("code-559", "title-268")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
