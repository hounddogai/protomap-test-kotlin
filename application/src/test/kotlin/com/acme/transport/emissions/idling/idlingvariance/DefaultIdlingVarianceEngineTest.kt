package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultIdlingVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultIdlingVarianceEngine(newRegistry())
        val item = newDefaultIdlingVariance("code-717", "name-511")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
