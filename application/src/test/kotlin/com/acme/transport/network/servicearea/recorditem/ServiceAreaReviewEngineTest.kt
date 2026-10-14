package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaReviewEngine(newRegistry())
        val item = newServiceAreaReview("code-640", "title-778")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
