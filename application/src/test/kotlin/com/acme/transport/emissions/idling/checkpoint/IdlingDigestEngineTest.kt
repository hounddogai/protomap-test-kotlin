package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingDigestEngine(newRegistry())
        val item = newIdlingDigest("code-992", "label-921")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
