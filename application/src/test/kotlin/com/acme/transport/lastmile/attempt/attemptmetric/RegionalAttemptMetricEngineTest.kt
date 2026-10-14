package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalAttemptMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalAttemptMetricEngine(newRegistry())
        val item = newRegionalAttemptMetric("code-3", "title-728")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
