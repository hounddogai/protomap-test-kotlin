package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptRollupEngine(newRegistry())
        val item = newAttemptRollup("code-858", "title-835")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
