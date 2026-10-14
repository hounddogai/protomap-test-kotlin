package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictAttemptOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictAttemptOverrideEngine(newRegistry())
        val item = newStrictAttemptOverride("code-827", "label-466")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
