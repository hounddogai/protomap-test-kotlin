package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientServiceAreaRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientServiceAreaRollupEngine()
        val item = newLenientServiceAreaRollup("code-840", "title-432")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
