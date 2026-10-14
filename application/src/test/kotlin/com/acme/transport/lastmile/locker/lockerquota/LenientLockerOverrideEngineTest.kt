package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientLockerOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientLockerOverrideEngine()
        val item = newLenientLockerOverride("code-699", "title-629")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
