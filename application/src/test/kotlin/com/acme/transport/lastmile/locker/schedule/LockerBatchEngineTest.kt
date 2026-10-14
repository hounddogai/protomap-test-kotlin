package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LockerBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerBatchEngine(newRegistry())
        val item = newLockerBatch("code-570", "label-921")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
