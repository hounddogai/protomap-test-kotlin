package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerReviewEngine()
        val item = newLockerReview("code-955", "label-117")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
