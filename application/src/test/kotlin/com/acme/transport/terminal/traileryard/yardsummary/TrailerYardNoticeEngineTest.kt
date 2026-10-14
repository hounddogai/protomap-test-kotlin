package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardNoticeEngine(newRegistry())
        val item = newTrailerYardNotice("code-499", "title-193")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
