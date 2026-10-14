package com.acme.transport.linehaul.relay.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayTemplateEngine()
        val item = newRelayTemplate("code-79", "label-326")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
