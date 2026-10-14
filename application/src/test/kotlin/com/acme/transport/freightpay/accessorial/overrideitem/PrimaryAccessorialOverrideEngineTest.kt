package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryAccessorialOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryAccessorialOverrideEngine()
        val item = newPrimaryAccessorialOverride("code-11", "name-352")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
