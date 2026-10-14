package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellReviewEngine(newRegistry())
        val item = newDwellReview("code-977", "title-668")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
