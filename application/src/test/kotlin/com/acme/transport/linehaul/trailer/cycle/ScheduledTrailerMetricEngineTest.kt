package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTrailerMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledTrailerMetricEngine(newRegistry())
        val item = newScheduledTrailerMetric("code-713", "title-928")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
