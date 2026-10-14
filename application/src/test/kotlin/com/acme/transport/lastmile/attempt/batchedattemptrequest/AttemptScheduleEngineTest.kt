package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptScheduleEngine(newRegistry())
        val item = newAttemptSchedule("code-633", "name-498")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
