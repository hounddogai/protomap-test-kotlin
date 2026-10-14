package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerSegmentEngine()
        val item = newLockerSegment("code-309", "label-646")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
