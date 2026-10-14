package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAttemptRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAttemptRequestEngine(newRegistry())
        val item = newTieredAttemptRequest("code-576", "label-813")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
