package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingServiceAreaEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingServiceAreaEntryEngine()
        val item = newPendingServiceAreaEntry("code-146", "title-22")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
