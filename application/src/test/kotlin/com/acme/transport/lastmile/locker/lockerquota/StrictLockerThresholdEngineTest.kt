package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictLockerThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictLockerThresholdEngine()
        val item = newStrictLockerThreshold("code-178", "name-823")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
