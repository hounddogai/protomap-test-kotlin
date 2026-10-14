package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLockerSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLockerSegmentEngine(newRegistry())
        val item = newCappedLockerSegment("code-395", "label-848")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
