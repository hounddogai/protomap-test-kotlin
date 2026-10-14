package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerMappingEngine()
        val item = newLockerMapping("code-565", "label-298")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
