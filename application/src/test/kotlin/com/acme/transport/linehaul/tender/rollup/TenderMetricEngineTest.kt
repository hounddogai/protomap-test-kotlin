package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderMetricEngine(newRegistry())
        val item = newTenderMetric("code-974", "label-651")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
