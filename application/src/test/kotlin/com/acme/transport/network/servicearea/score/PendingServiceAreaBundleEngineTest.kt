package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingServiceAreaBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingServiceAreaBundleEngine(newRegistry())
        val item = newPendingServiceAreaBundle("code-382", "name-893")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
