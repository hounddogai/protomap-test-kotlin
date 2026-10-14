package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientAccessorialMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientAccessorialMetricEngine(newRegistry())
        val item = newLenientAccessorialMetric("code-568", "label-392")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
