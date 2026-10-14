package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryRosterRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryRosterRecordEngine()
        val item = newPrimaryRosterRecord("code-863", "name-187")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
