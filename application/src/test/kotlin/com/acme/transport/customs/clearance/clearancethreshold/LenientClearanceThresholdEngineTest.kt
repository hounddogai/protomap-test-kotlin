package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientClearanceThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientClearanceThresholdEngine(newRegistry())
        val item = newLenientClearanceThreshold("code-523", "label-785")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
