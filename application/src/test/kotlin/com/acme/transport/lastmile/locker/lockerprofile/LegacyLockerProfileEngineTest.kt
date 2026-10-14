package com.acme.transport.lastmile.locker.lockerprofile

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLockerProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLockerProfileEngine()
        val item = newLegacyLockerProfile("code-807", "name-643")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
