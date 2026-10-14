package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptNoticeEngine(newRegistry())
        val item = newAttemptNotice("code-387", "label-891")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
