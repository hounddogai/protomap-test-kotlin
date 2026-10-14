package com.acme.transport.lastmile.locker.group

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerBaselineEngine()
        val item = newLockerBaseline("code-272", "title-178")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
