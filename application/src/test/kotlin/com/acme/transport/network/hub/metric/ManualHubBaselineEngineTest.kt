package com.acme.transport.network.hub.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualHubBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualHubBaselineEngine()
        val item = newManualHubBaseline("code-767", "title-517")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
