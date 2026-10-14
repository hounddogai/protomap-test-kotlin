package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalLockerRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalLockerRecordEngine()
        val item = newGlobalLockerRecord("code-117", "name-161")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
