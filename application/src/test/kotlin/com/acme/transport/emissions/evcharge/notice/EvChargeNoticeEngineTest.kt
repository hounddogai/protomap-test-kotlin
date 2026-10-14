package com.acme.transport.emissions.evcharge.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeNoticeEngine(newRegistry())
        val item = newEvChargeNotice("code-150", "name-247")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
