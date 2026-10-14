package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRosterBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRosterBaselineEngine(newRegistry())
        val item = newStrictRosterBaseline("code-707", "title-19")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
