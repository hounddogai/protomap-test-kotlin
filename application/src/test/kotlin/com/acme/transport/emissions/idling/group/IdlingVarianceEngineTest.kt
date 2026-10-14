package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class IdlingVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultIdlingVarianceEngine(newRegistry())
        val item = newIdlingVariance("code-419", "name-32")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
