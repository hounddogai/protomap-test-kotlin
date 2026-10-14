package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaOverrideEngine(newRegistry())
        val item = newServiceAreaOverride("code-939", "title-685")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
