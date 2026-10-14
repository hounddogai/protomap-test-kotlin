package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTrailerVarianceEngine(newRegistry())
        val item = newManualTrailerVariance("code-39", "name-175")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
