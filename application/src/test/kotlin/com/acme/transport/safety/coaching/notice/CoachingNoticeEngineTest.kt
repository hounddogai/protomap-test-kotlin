package com.acme.transport.safety.coaching.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingNoticeEngine()
        val item = newCoachingNotice("code-319", "name-453")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
