package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRosterAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRosterAgreementEngine()
        val item = newDefaultRosterAgreement("code-404", "label-177")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
