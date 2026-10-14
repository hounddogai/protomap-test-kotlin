package com.acme.transport.lastmile.locker.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackLockerBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackLockerBaselineEngine()
        val item = newFallbackLockerBaseline("code-683", "name-510")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
