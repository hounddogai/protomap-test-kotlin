package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCoachingNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCoachingNoticeEngine(newRegistry())
        val item = newManualCoachingNotice("code-289", "title-76")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
