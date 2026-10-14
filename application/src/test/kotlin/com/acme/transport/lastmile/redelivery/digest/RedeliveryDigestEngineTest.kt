package com.acme.transport.lastmile.redelivery.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryDigestEngine()
        val item = newRedeliveryDigest("code-651", "title-995")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
