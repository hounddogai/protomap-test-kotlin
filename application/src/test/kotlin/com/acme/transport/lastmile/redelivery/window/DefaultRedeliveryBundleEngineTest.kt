package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultRedeliveryBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultRedeliveryBundleEngine(newRegistry())
        val item = newDefaultRedeliveryBundle("code-943", "name-706")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
