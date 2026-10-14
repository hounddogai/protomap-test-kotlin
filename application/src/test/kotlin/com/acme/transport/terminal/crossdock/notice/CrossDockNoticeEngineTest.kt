package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockNoticeEngine()
        val item = newCrossDockNotice("code-719", "name-514")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
