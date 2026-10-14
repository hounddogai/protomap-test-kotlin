package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDwellReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDwellReviewEngine()
        val item = newLenientDwellReview("code-824", "label-889")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
