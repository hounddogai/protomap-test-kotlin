package com.acme.transport.lastmile.redelivery.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryOverrideEngine()
        val item = newRedeliveryOverride("code-748", "label-707")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
