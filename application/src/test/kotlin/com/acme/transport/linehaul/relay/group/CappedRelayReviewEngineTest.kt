package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedRelayReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedRelayReviewEngine(newRegistry())
        val item = newCappedRelayReview("code-273", "name-408")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
