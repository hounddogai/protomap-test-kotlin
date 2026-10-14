package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerNoticeEngine(newRegistry())
        val item = newTrailerNotice("code-756", "name-317")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
