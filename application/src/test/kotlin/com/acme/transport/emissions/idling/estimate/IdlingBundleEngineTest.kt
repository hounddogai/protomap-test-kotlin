package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingBundleEngine(newRegistry())
        val item = newIdlingBundle("code-200", "label-375")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
