package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingNoticeEngine(newRegistry())
        val item = newIdlingNotice("code-715", "title-194")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
