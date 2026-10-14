package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayNoticeEngine(newRegistry())
        val item = newRelayNotice("code-529", "label-662")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
