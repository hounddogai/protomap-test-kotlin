package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledNearMissRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledNearMissRecordEngine(newRegistry())
        val item = newScheduledNearMissRecord("code-57", "label-568")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
