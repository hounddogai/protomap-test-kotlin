package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaThresholdEngine(newRegistry())
        val item = newServiceAreaThreshold("code-823", "name-768")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
