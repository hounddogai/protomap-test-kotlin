package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultLockerMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultLockerMetricEngine()
        val item = newDefaultLockerMetric("code-250", "label-942")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
