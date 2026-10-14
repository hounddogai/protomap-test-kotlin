package com.acme.transport.lastmile.locker.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalLockerBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalLockerBatchEngine()
        val item = newGlobalLockerBatch("code-703", "name-117")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
