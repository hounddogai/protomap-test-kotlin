package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayRecordEngine()
        val item = newRelayRecord("code-502", "title-168")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
