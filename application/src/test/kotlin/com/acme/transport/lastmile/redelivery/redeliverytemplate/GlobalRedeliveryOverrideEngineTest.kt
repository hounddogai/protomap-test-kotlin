package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRedeliveryOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRedeliveryOverrideEngine(newRegistry())
        val item = newGlobalRedeliveryOverride("code-55", "label-15")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
