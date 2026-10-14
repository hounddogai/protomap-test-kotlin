package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRosterAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRosterAdjustmentEngine(newRegistry())
        val item = newGlobalRosterAdjustment("code-704", "title-771")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
