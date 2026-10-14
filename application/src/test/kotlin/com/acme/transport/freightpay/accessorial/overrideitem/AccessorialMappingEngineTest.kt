package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialMappingEngine(newRegistry())
        val item = newAccessorialMapping("code-567", "label-451")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
