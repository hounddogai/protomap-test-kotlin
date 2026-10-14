package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRosterExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRosterExceptionEngine()
        val item = newTieredRosterException("code-892", "title-114")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
