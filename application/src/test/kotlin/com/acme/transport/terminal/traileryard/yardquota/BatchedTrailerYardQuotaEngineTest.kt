package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTrailerYardQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTrailerYardQuotaEngine(newRegistry())
        val item = newBatchedTrailerYardQuota("code-215", "title-997")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
