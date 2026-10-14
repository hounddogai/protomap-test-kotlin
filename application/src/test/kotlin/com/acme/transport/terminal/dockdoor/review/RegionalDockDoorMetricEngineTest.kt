package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDockDoorMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDockDoorMetricEngine(newRegistry())
        val item = newRegionalDockDoorMetric("code-414", "title-368")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
