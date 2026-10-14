package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCameraEventProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCameraEventProfileEngine(newRegistry())
        val item = newRegionalCameraEventProfile("code-373", "title-128")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
