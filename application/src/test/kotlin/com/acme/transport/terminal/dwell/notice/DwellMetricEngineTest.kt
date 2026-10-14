package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellMetricEngine(newRegistry())
        val item = newDwellMetric("code-322", "label-7")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
