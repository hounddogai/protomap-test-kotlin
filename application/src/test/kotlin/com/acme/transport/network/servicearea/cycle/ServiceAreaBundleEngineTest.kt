package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaBundleEngine(newRegistry())
        val item = newServiceAreaBundle("code-757", "title-618")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
