package com.acme.transport.lastmile.locker.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerOverrideEngine()
        val item = newLockerOverride("code-738", "label-205")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
