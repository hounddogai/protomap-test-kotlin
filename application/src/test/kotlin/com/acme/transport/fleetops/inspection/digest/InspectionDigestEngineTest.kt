package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionDigestEngine(newRegistry())
        val item = newInspectionDigest("code-668", "label-459")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
